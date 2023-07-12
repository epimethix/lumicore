/*
 * Copyright 2022 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.common.ioc;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.DatabaseApplication;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.Repository;

public interface Injector {
	
	Application getApplication();

	Object getComponent(Class<?> cls)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	Collection<Class<?>> searchClassesAssignableFrom(Class<?> cls);

	Collection<Class<?>> searchClassesByAnnotation(Class<? extends Annotation> cls);

	void registerComponent(Class<?> cls, Object instance, Collection<Class<?>> components);

	void putImplementation(Class<?> implementationClass);

	void putInterfaceImplementation(Class<?> interface0, Class<?> implementationClass);

	Object autowire(Class<?> cls, Object classInstance);

	<T> T autoInstance(Class<T> cls)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	String getApplicationPackage();

//	Database initializeDatabase(DatabaseApplication applicationInstance,
//			Class<? extends DatabaseApplication> applicationClass, Class<? extends Database> dbClass, File databaseFile)
//			throws ConfigurationException, InstantiationException, IllegalAccessException, IllegalArgumentException,
//			InvocationTargetException;

	Collection<Class<?>> searchClassesAssignableFrom(String value, Class<?> cls);

	void autowire(Collection<Class<?>> components) throws ConfigurationException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException;

//	void putImplementation(Class<?> implementationClass);
}
