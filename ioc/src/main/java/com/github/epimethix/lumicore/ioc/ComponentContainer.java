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
package com.github.epimethix.lumicore.ioc;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.management.RuntimeErrorException;

import com.github.epimethix.lumicore.common.ApplicationUtils;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.ioc.annotation.Qualifier;
import com.github.epimethix.lumicore.ioc.interception.InterceptionController;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;

public class ComponentContainer {

	private final Logger iocLogger = Log.getLogger(Log.CHANNEL_IOC);
	/**
	 * Key is database path
	 */
	private final Map<String, Database> databases = new HashMap<>();
	/**
	 * first key is database path, second key is entity class
	 */
	private final Map<String, Map<Class<?>, Repository<?, ?>>> repositories = new HashMap<>();
	/**
	 * Key is: ClassName:Qualifier or ClassName:SimpleClassName if there is no
	 * Qualifier
	 */
	private final Map<String, Object> components = new HashMap<>();

	private final Map<Class<?>, Map<String, Class<?>>> interfaces = new HashMap<>();

	private final Map<Class<?>, Class<?>> diMap = new HashMap<>();

	private final Map<Class<?>, Object> applicationScope = new HashMap<>();
	private final Map<Class<?>, Object> proxyScope = new HashMap<>();

	private final Map<String, InterceptionController> interceptionControllers = new HashMap<>();

	public final void registerInterceptionController(InterceptionController ic) {
		interceptionControllers.put(ic.getName(), ic);
	}

	public final void registerComponent(Object component) {
		if (Proxy.isProxyClass(component.getClass())) {
			
		}
		String qualifier;
		Class<?> implementationClass = component.getClass();
		if (implementationClass.isAnnotationPresent(Qualifier.class)) {
			qualifier = implementationClass.getAnnotation(Qualifier.class).value().toLowerCase();
		} else {
			qualifier = implementationClass.getSimpleName();
		}
//		for (Class<?> intrfc : implementationClass.getInterfaces()) {
//			Map<String, Class<?>> ifMap = interfaces.get(intrfc);
//			if (Objects.isNull(ifMap)) {
//				ifMap = new HashMap<>();
//				interfaces.put(intrfc, ifMap);
//			}
//			ifMap.put(qualifier, implementationClass);
//		}
		diMap.put(implementationClass, implementationClass);
		applicationScope.put(implementationClass, component);
		qualifier = implementationClass.getName() + ":" + qualifier;
		components.put(qualifier, implementationClass);
	}

	public final void registerDatabase(Database database) {
		databases.put(database.getPath(), database);
	}

	public final void registerRepository(Database database, Repository<?, ?> repository) {
		Map<Class<?>, Repository<?, ?>> repoMap = repositories.get(database.getPath());
		if (Objects.isNull(repoMap)) {
			repoMap = new HashMap<>();
			repositories.put(database.getPath(), repoMap);
		}
		repoMap.put(repository.getEntityClass(), repository);
	}

	Class<?> getImplementationClass(Class<?> type, String name, String qualifier) {
		String selector = (Objects.isNull(qualifier) || qualifier.trim().isEmpty() ? name : qualifier).toLowerCase();

		Class<?> implementationClass = null;
		if (type.isInterface()) {
			Map<String, Class<?>> ifMap = interfaces.get(type);
			if (Objects.nonNull(ifMap)) {
				implementationClass = ifMap.get(selector);
				if (Objects.isNull(implementationClass)) {
					Set<?> keySet = ifMap.keySet();
					if (keySet.size() == 1) {
						implementationClass = ifMap.get(keySet.iterator().next());
					}
				}
			}
		} else {
			implementationClass = diMap.get(type);
		}
		if (Objects.nonNull(implementationClass)) {
			return implementationClass;
		}
		String errorMessage = "Implementation class not found: " + type.getSimpleName();
//		System.err.println(errorMessage);
		iocLogger.error(errorMessage);
		throw new RuntimeErrorException(new Error(errorMessage));
	}

	public void putInterfaceImplementation(Class<?> interface0, Class<?> implementationClass) {
		String qualifier;
		if (implementationClass.isAnnotationPresent(Qualifier.class)) {
			qualifier = implementationClass.getAnnotation(Qualifier.class).value().toLowerCase();
		} else {
			qualifier = implementationClass.getSimpleName().toLowerCase();
		}
		Map<String, Class<?>> ifMap = interfaces.get(interface0);
		if (Objects.isNull(ifMap)) {
			ifMap = new HashMap<>();
			ifMap.put(qualifier, implementationClass);
			interfaces.put(interface0, ifMap);
		} else {
			ifMap.put(qualifier, implementationClass);
		}
	}

	public boolean applicationScopeContainsKey(Class<?> cls) {
		return applicationScope.containsKey(cls);
	}

	public Object get(Class<?> cls) {
		return applicationScope.get(cls);
	}

	public void buildDiMap(Collection<Class<?>> components) {
		for (Class<?> cls : components) {
			diMap.put(cls, cls);
			Class<?>[] interfaces = cls.getInterfaces();
			for (Class<?> interface0 : interfaces) {
				putInterfaceImplementation(interface0, cls);
			}
		}
	}
	
	public void logInterfaceMap() {
		StringBuilder diagnostics = new StringBuilder();
		diagnostics.append("\n");
		diagnostics.append(ApplicationUtils.createBanner("Interface Map"));
		diagnostics.append("\n");
		diagnostics.append("\n");
		List<Class<?>> ifKeys = new ArrayList<>(interfaces.keySet());
		Collections.sort(ifKeys, (a, b) -> a.getName().compareTo(b.getName()));
		for (Class<?> key : ifKeys) {
			diagnostics.append("Interface '").append(key.getSimpleName()).append("'\n");
			Map<String, Class<?>> impls = interfaces.get(key);
			List<String> implsKeys = new ArrayList<>(impls.keySet());
			Collections.sort(implsKeys);
			for (String qualifier : implsKeys) {
				diagnostics.append("\t\t"+qualifier+"="+impls.get(qualifier).getSimpleName()).append(", ");
			}
			diagnostics.append("\n");
		}
		iocLogger.info(diagnostics.toString());
	}
}
