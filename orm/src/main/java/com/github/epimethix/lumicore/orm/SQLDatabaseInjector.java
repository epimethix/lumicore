/*
 * Copyright 2023 epimethix@protonmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.epimethix.lumicore.orm;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.epimethix.lumicore.benchmark.Benchmark;
import com.github.epimethix.lumicore.benchmark.Benchmark.Check;
import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.DatabaseApplication;
import com.github.epimethix.lumicore.common.Reflect;
import com.github.epimethix.lumicore.common.ioc.Injector;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.DatabaseInjector;
import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.model.TreeEntity;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;
import com.github.epimethix.lumicore.orm.annotation.database.Repositories;
import com.github.epimethix.lumicore.orm.annotation.field.LazyLoad;

public class SQLDatabaseInjector implements DatabaseInjector {

	private final Injector injector;
	private final Logger ormLogger = Log.getLogger(Log.CHANNEL_ORM);

	public SQLDatabaseInjector(Injector injector) {
		this.injector = injector;
	}

	@Override
	public void initializeDatabases(Class<? extends DatabaseApplication> applicationClass, DatabaseApplication appInstance,
			Collection<Class<?>> components) throws ConfigurationException, InvocationTargetException {

		Collection<Class<?>> dbClasses = injector.searchClassesAssignableFrom(Database.class);
		Collection<Class<?>> nonIFdbClasses = new ArrayList<>();
		for (Class<?> cls : dbClasses) {
			if (!cls.isInterface()) {
				nonIFdbClasses.add(cls);
			}
		}
		if (nonIFdbClasses.size() > 1) {
			for (Class<?> dbClass : nonIFdbClasses) {
				if (!dbClass.isAnnotationPresent(Repositories.class)) {
					throw new ConfigurationException(
							ConfigurationException.MULTIPLE_DB_CLASSES_NEED_REPOSITORIES_ANNOTATION,
							dbClass.getSimpleName());
				}
			}
		}
		for (Class<?> dbClass : nonIFdbClasses) {
			if (dbClass.isAnnotationPresent(LazyLoad.class)) {
				continue;
			}
			Check ckInitializeDatabaseInstance = Benchmark.start(SQLDatabaseInjector.class,
					"initializeDatabaseInstance(" + dbClass.getSimpleName() + ")");
			Database dbInstance = initializeDatabaseInstance(dbClass, applicationClass, appInstance, components, null);
			ckInitializeDatabaseInstance.stop();
			if (Objects.isNull(dbInstance)) {
				throw new ConfigurationException(ConfigurationException.INITIALIZE_DATABASE_FAILED,
						dbClass.getSimpleName());
			} else {
				injector.registerComponent(dbClass, dbInstance, components);
			}
		}
	}

	private Database initializeDatabaseInstance(Class<?> dbClass, Class<? extends DatabaseApplication> applicationClass,
			Object appInstance, Collection<Class<?>> components, File databaseFile)
			throws ConfigurationException, InvocationTargetException {
		Database dbInstance = null;
		try {
//			if(dbClass.isInterface()) {
//				dbClass = getImplementationClass(dbClass, "", null);
//			}
			Constructor<?>[] dbConstructors = dbClass.getConstructors();
			for (Constructor<?> c : dbConstructors) {
				if (Objects.nonNull(databaseFile)) {
					if (c.getParameterCount() == 2) {
						if (Reflect.typeEqualsOneOf(c.getParameterTypes()[0], DatabaseApplication.class,
								appInstance.getClass()) && Reflect.typeEquals(c.getParameterTypes()[1], File.class)) {
							dbInstance = (Database) c.newInstance(appInstance, databaseFile);
							break;
						}

					}
				} else {
					if (c.getParameterCount() == 1) {
						if (Reflect.typeEqualsOneOf(c.getParameterTypes()[0], DatabaseApplication.class,
								appInstance.getClass())) {
							dbInstance = (Database) c.newInstance(appInstance);
							break;
						}
					}
				}
			}
			if (Objects.nonNull(dbInstance)) {
				Check ckInitializeRepositories = Benchmark.start(SQLDatabaseInjector.class,
						dbInstance.getClass().getSimpleName() + ".initializeRepositories");
				initializeRepositories(applicationClass, dbInstance, components);
				ckInitializeRepositories.stop();
				try {
					Check ckAutosync = Benchmark.start(SQLDatabaseInjector.class,
							dbClass.getSimpleName() + ".autoSyncSchema()");
					dbInstance.autoSyncSchema();
					ckAutosync.stop();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return dbInstance;
	}

	private void initializeRepositories(Class<? extends DatabaseApplication> applicationClass, Database dbInstance,
			Collection<Class<?>> components) throws ConfigurationException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Repositories repositories = dbInstance.getClass().getAnnotation(Repositories.class);
		Collection<Class<?>> repoClasses;
		if (Objects.nonNull(repositories)) {
			repoClasses = injector.searchClassesAssignableFrom(repositories.value(), Repository.class);
		} else {
			repoClasses = injector.searchClassesAssignableFrom(Repository.class);
		}
		Collection<Class<?>> nonIFRepositoryClasses = new HashSet<>();
		for (Class<?> repoClass : repoClasses) {
			if (!repoClass.isInterface()) {
				nonIFRepositoryClasses.add(repoClass);
			}
		}
		repoClasses = nonIFRepositoryClasses;
		Check ckPrepareEntities = Benchmark.start(SQLDatabaseInjector.class,
				dbInstance.getClass().getSimpleName() + ".initializeRepositories:prepare");
		/**
		 * map repository classes to entity classes
		 */
		Map<Class<? extends Entity<?>>, Class<?>> repoMap = new HashMap<Class<? extends Entity<?>>, Class<?>>();
//		Map<Class<? extends Entity<?>>, Class<?>> parentRepoMap = new HashMap<Class<? extends Entity<?>>, Class<?>>();
		List<Class<? extends Entity<?>>> treeParentRepositories = new ArrayList<>();
		for (Class<?> repoClass : repoClasses) {
//			System.out.println(
//					Arrays.toString(Reflect.getActualTypeArgumentsFromGenericInterface(repoClass, Repository.class)));
			Class<? extends Entity<?>> entityClass = Reflect.getEntityClass(repoClass);
			repoMap.put(entityClass, repoClass);
			if (TreeEntity.class.isAssignableFrom(entityClass)) {
				Class<?>[] t = Reflect.getActualTypeArgumentsFromGenericInterface(entityClass, TreeEntity.class);
				if (Objects.nonNull(t) && Reflect.typeEquals(t[0], entityClass)) {
					treeParentRepositories.add(entityClass);
				}
			}
		}

		List<Class<? extends Entity<?>>> entityClasses = new ArrayList<Class<? extends Entity<?>>>(repoMap.keySet());
		/**
		 * Sort entity classes
		 */

		ORM.sortEntityClasses(entityClasses);

		ckPrepareEntities.stop();

		/**
		 * initialize repositories in order
		 */

		for (Class<? extends Entity<?>> entityClass : entityClasses) {
			Class<?> repoClass = repoMap.get(entityClass);
			Check ckInitializeRepository = Benchmark.start(SQLDatabaseInjector.class,
					"initializeRepository(" + repoClass.getSimpleName() + ")",
					"repositories(" + dbInstance.getClass().getSimpleName() + ")");
			Repository<?, ?> repoInstance = initializeRepository(repoClass, dbInstance);
			ckInitializeRepository.stop();
			if (Objects.isNull(repoInstance)) {
				throw new ConfigurationException(ConfigurationException.INITIALIZE_REPOSITORY_FAILED,
						repoClass.getSimpleName());
			} else {
				ormLogger.info("%n%s", repoInstance.toString());
				dbInstance.registerRepository(repoInstance);
				EntityController.register(entityClass, repoInstance);
				injector.registerComponent(repoClass, repoInstance, components);
			}
		}

		for (Class<? extends Entity<?>> parentClass : treeParentRepositories) {
			Repository<?, ?> parentRepo = EntityController.getRepository(parentClass);
			for (Class<? extends Entity<?>> entityClass : entityClasses) {
				if (TreeEntity.class.isAssignableFrom(entityClass) && !Reflect.typeEquals(entityClass, parentClass)) {
					Class<?>[] t = Reflect.getActualTypeArgumentsFromGenericInterface(entityClass, TreeEntity.class);
					if (Objects.nonNull(t) && Reflect.typeEquals(parentClass, t[0])) {
						Repository<?, ?> childRepo = EntityController.getRepository(entityClass);
						parentRepo.registerChildRepository(childRepo);
					}
				}
			}
		}
	}

	private Repository<?, ?> initializeRepository(Class<?> repoClass, Database dbInstance)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Repository<?, ?> repoInstance = null;
		Constructor<?>[] constructors = repoClass.getConstructors();
		for (Constructor<?> constructor : constructors) {
			if (constructor.getParameterCount() == 1) {
				Parameter param = constructor.getParameters()[0];
				if (Reflect.typeEqualsOneOf(param.getType(), Database.class, dbInstance.getClass())) {
					repoInstance = (Repository<?, ?>) constructor.newInstance(dbInstance);
				}
			}
		}
		return repoInstance;
	}

	@Override
	public Database initializeDatabase(DatabaseApplication applicationInstance,
			Class<? extends DatabaseApplication> applicationClass, Class<? extends Database> dbClass, File databaseFile)
			throws ConfigurationException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Collection<Class<?>> components = new HashSet<>();

		Database db = initializeDatabaseInstance(dbClass, applicationClass, applicationInstance, components,
				databaseFile);
		injector.autowire(components);
		return db;
	}

//	public static Database loadDatabase(DatabaseApplication applicationInstance,
//			Class<? extends DatabaseApplication> applicationClass, Class<? extends Database> dbClass, File databaseFile)
//			throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException,
//			InvocationTargetException, ConfigurationException {
//		if (Objects.isNull(injector)) {
//			injector = new Lumicore(applicationClass);
//		}
//		return injector.initializeDatabase(applicationInstance, applicationClass, dbClass, databaseFile);
//	}
}
