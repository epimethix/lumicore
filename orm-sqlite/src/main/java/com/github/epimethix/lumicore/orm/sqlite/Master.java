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

/**
 * Class containing SQLite queries to inspect the "sqlite_master"
 */
public final class Master {
	/**
	 * Entity name: "sqlite_master"
	 */
	public static final String ENTITY_NAME = "sqlite_master";
	/**
	 * field name: sqlite_master.type
	 */
	public static final String TYPE = "type";
	/**
	 * field name: sqlite_master.name
	 */
	public static final String NAME = "name";
	/**
	 * field name: sqlite_master.tbl_name
	 */
	public static final String TBL_NAME = "tbl_name";
	/**
	 * field name: sqlite_master.rootpage
	 */
	public static final String ROOTPAGE = "rootpage";
	/**
	 * field name: sqlite_master.sql
	 */
	public static final String SQL = "sql";

	/**
	 * Array containing the column names of the "sqlite_master"
	 */
	public final static String[] COLUMNS = { TYPE, NAME, TBL_NAME, ROOTPAGE, SQL };

	// @formatter:off
	/**
	 * SQL String / Query SELECT all fields
	 */
	public final static String SQL_MASTER_SELECT_ALL;
	
	/**
	 * SQL String / Query SELECT sqlite_master.name of non system tables (NOT LIKE 'sqlite_%').
	 * <p>
	 * Ordered by sqlite_master.name
	 */
	public final static String SQL_MASTER_SELECT_NAMES_OF_NON_SYSTEM_TABLES = 
			"SELECT `name` FROM `sqlite_master` WHERE `type`='table' AND `name` NOT LIKE 'sqlite_%' ORDER BY `name`";
	
	/**
	 * SQL format String / Prepared Statement / Query SELECT all fields WHERE %s=?
	 */
	private final static String SQL_MASTER_SELECT_BY_FORMAT;
	
	static {
		SQLiteBuilder bSelectAll = SQLiteBuilder.newBuilder()
				.selectFrom(Master.ENTITY_NAME, Master.COLUMNS);
		SQL_MASTER_SELECT_ALL = bSelectAll.getSQL();
		
		SQLiteBuilder bSelectByFormat = SQLiteBuilder.newBuilder(bSelectAll)
				.where("sqlite_master", "%s");
		SQL_MASTER_SELECT_BY_FORMAT = bSelectByFormat.getSQL();

	}
	// @formatter:on

	/**
	 * SQL format String / Prepared Statement / Query SELECT all fields WHERE
	 * sqlite_master.name=?
	 */
	public final static String SQL_MASTER_SELECT_BY_NAME = sqlMasterSelectBy(NAME);

	/**
	 * get the SQL String / Query SELECT all fields WHERE fieldName=?
	 * 
	 * @param fieldName the field name (sqlite_master.?) to select by
	 * 
	 * @return the SQL query to select from sqlite_master by the specified fieldName
	 */
	public final static String sqlMasterSelectBy(String fieldName) {
//		System.out.println(SQL_MASTER_SELECT_BY_FORMAT);
		return String.format(SQL_MASTER_SELECT_BY_FORMAT, fieldName);
	}

	private Master() {}
} // End of class master
