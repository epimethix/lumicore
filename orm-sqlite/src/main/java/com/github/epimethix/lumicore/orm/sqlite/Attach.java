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
package com.github.epimethix.lumicore.orm.sqlite;

public class Attach {

//	attach 'database1.db' as db1;
//	attach 'database2.db' as db2;
	// TODO Test connections to multiple database files?
	private final static String ATTACH_FORMAT = "ATTACH DATABASE '%s' AS %s";
	private final static String DETACH_FORMAT = "DETACH DATABASE %s";
//	.databases
	public final static String DATABASES = ".DATABASES";

	/**
	 * Generate DETACH command.
	 * <p>
	 * Danger Zone! Vulnerable to SQL injection!
	 * 
	 * @param schemaName Danger Zone! Vulnerable to SQL injection! the currently
	 *                   attached schema
	 * @return the SQL query String
	 */
	public static final String sqlDetach(String schemaName) {
		return String.format(DETACH_FORMAT, schemaName);
	}

	/**
	 * Generate ATTACH command.
	 * <p>
	 * Danger Zone! Vulnerable to SQL injection!
	 * 
	 * @param dbLocation
	 * @param schemaName Danger Zone! Vulnerable to SQL injection! the currently
	 *                   attached schema
	 * @return the SQL query String
	 * @return
	 */
	public static final String sqlAttach(String dbLocation, String schemaName) {
		return String.format(ATTACH_FORMAT, dbLocation, schemaName);
	}
}
