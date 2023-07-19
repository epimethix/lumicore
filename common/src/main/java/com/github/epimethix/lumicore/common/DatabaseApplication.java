/*
 * Copyright 2021-2023 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.common;

import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.sql.ConnectionFactory;
import com.github.epimethix.lumicore.ioc.annotation.ComponentScan;
import com.github.epimethix.lumicore.ioc.annotation.JarFileScan;

/**
 * The {@code DatabaseApplication} interface can be implemented to define an
 * application class.
 * <p>
 * Use {@code DatabaseApplication} to specify an application with a database.
 * <p>
 * The application class, by convention, is located in the root package of the
 * project. The component scan uses the package of the application class as root
 * package to scan. If other packages should be scanned too or if the
 * application class does not reside in the root package then the annotation
 * {@code ComponentScan} can be used to specify the packages to scan.
 * <p>
 * If the project is being deployed as a jar file with sibling jar files to scan
 * the annotation {@code JarFileScan} can be used to specify additional jars to
 * scan.
 * 
 * @author epimethix
 * 
 * @see Application
 * @see ComponentScan
 * @see JarFileScan
 */
public interface DatabaseApplication extends Application {
	/**
	 * Gets the current application version
	 * 
	 * @return the current application version
	 */
	long getApplicationVersion();

	/**
	 * Gets the minimally required application version for the current database
	 * structure.
	 * 
	 * @return the currently required application version.
	 */
	default long getRequiredApplicationVersion() {
		return getApplicationVersion();
	}

	/**
	 * Gets the application id to identify compatible database files.
	 * <p>
	 * The implementation of this method can return zero (0) to disable the
	 * compatibility check or the 'unique' application id.
	 * 
	 * @return the application (database type) id
	 */
	int getApplicationId();

//	SQLConnection initializeConnection(Database database);
	
	ConnectionFactory createConnectionFactory(Class<? extends Database> dbClass);
	
	
}
