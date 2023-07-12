/*
 * Copyright 2021-2022 epimethix@protonmail.com
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

/**
 * The DatabaseApplication interface is created to secure the database against
 * unwanted/malicious down-grades when in case for whatever reason an out-dated
 * version of the application tries to access the database.
 * 
 * <pre>
 * 
 * class MyApplication extends AbstractDatabaseApplication {
 * 	
 * 	...
 * 	
 * 	&#64;Override
 * 	public long getApplicationVersion() {
 * 		// representing VersionHistory.V5_FIFTH_VERSION_NAME for future application versions
 * 		return VersionHistory.CURRENT_VERSION;
 * 	}
 * 
 * 	&#64;Override
 * 	public long getCurrentlyRequiredApplicationVersion() {
 * 		/*
 * 		 * assuming the last database structure upgrade was when upgrading
 * 		 * to application version VersionHistory.V2_SECOND_VERSION_NAME. 
 * 		 * If desired as a fall back strategy this way there is a the 
 * 		 * possibility of backwards-compatibility, at least considering 
 * 		 * application releases that did not alter the database structure.
 * 		 * /
 * 		return VersionHistory.V2_SECOND_VERSION_NAME;
 * 	}
 * 
 * 	... alternatively ...
 * 
 * 	&#64;Override
 * 	public long getCurrentlyRequiredApplicationVersion() {
 * 		/*
 * 		 * To always enforce usage of the latest version 
 * 		 * and deny all older versions access to the file... 
 * 		 * /
 * 		// (TODO (NIY): in what case does the required app version need to be updated??)
 * 		return getApplicationVersion();
 * 	}
 * 
 * }
 * 
 * ....
 * 
 * class VersionHistory {
 * 	public final static long CURRENT_VERSION = BuildConfig.VERSION;
 * 	
 * 	public final static long V1_FIRST_VERSION_NAME = 2;
 * 	public final static long V2_SECOND_VERSION_NAME = 5;
 * 	public final static long V3_THIRD_VERSION_NAME = 9;
 * 	public final static long V4_FOURTH_VERSION_NAME = 2500;
 * 	public final static long V5_FIFTH_VERSION_NAME = CURRENT_VERSION;
 * 
 * 	private VersionHistory() {}
 * }
 * 
 * </pre>
 * 
 * @author epimethix
 * 
 */
public abstract class AbstractDatabaseApplication implements DatabaseApplication {

	/**
	 * Instantiate the database application without lock file to enforce running a
	 * single instance of the app.
	 */
	public AbstractDatabaseApplication() {
//		this(null);
	}

	/**
	 * Instantiate the database application using the specified lock file to enforce
	 * running a single instance of the app.
	 * 
	 * @param lockFile the file to get locked by the only instance of the database
	 *                 application or null to allow multiple instances.
	 */
//	public AbstractDatabaseApplication(File lockFile) {
//		if (Objects.nonNull(lockFile)) {
//			ApplicationUtils.lockSingleInstance(lockFile, getApplicationName());
//		}
//	}
}
