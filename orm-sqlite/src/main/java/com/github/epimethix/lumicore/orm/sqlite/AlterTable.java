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

import com.github.epimethix.lumicore.common.orm.sqlite.Definition;

/**
 * SQLite ALTER TABLE vocabulary
 */
public final class AlterTable {
	/**
	 * SQL format String / ALTER TABLE %s ADD %s
	 */
	private static final String ALTER_TABLE_ADD_COLUMN_FORMAT = "ALTER TABLE `%s` ADD %s";
	/**
	 * SQL format String / ALTER TABLE %s RENAME TO %s
	 */
	private static final String ALTER_TABLE_RENAME_TO_FORMAT = "ALTER TABLE `%s` RENAME TO `%s`";

	/**
	 * Alter the specified table to add the defined column
	 * 
	 * @param tableName the table to alter
	 * @param d         the field definition to add
	 * 
	 * @return the ALTER TABLE statement to add the specified column
	 */
	public final static String sqlAlterTableAddColumn(String tableName, Definition d) {
		return String.format(ALTER_TABLE_ADD_COLUMN_FORMAT, tableName, d.toString());
	}

	/**
	 * Rename the specified table
	 * 
	 * @param tableName    the table name to alter
	 * @param newTableName the new table name
	 * 
	 * @return the SQL command to alter a tables name
	 */
	public final static String sqlAlterTableRenameTo(String tableName, String newTableName) {
		return String.format(ALTER_TABLE_RENAME_TO_FORMAT, tableName, newTableName);
	}

	private AlterTable() {}
} // end of class AlterTable

