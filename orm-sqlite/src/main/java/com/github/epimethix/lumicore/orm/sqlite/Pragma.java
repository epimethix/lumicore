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
 * Class containing some SQLite PRAGMA vocabulary
 */
public final class Pragma {
	private static final String GET_PRAGMA_FORMAT = "PRAGMA %s";
	private static final String SET_INTEGER_PRAGMA_FORMAT = "PRAGMA %s=%d";
	private static final String SET_STRING_PRAGMA_FORMAT = "PRAGMA %s='%s'";
	private static final String SET_HEX_PRAGMA_FORMAT = "PRAGMA %s=\"x'%s'\"";

	private final static String PRAGMA_SCHEMA_VERSION = "schema_version";
	private final static String PRAGMA_USER_VERSION = "user_version";
	private final static String PRAGMA_FOREIGN_KEYS = "foreign_keys";
	private final static String PRAGMA_LEGACY_ALTER_TABLE = "legacy_alter_table";

	private final static String PRAGMA_TABLE_INFO = "table_info";
	private final static String PRAGMA_APPLICATION_ID = "application_id";
	public final static String PRAGMA_KEY = "key";
	public final static String PRAGMA_REKEY = "rekey";

	/* @formatter:off
		analysis_limit 
		x	application_id 
		auto_vacuum 
		automatic_index 
		busy_timeout 
		cache_size 
		cache_spill 
		case_sensitive_like 
		cell_size_check 
		checkpoint_fullfsync 
		collation_list 
		compile_options 
		data_version 
		database_list 
		defer_foreign_keys 
		encoding 
		foreign_key_check 
		foreign_key_list 
		x	foreign_keys 
		freelist_count 
		fullfsync 
		function_list 
		hard_heap_limit 
		ignore_check_constraints 
		incremental_vacuum 
		index_info 
		index_list 
		index_xinfo 
		integrity_check 
		journal_mode 
		journal_size_limit 
		legacy_alter_table 
		legacy_file_format 
		locking_mode 
		max_page_count 
		mmap_size 
		module_list 
		optimize 
		page_count 
		page_size 
		pragma_list 
		query_only 
		quick_check 
		read_uncommitted 
		recursive_triggers 
		reverse_unordered_selects 
		secure_delete 
		shrink_memory 
		soft_heap_limit 
		synchronous 
		x	table_info 
		table_list 
		table_xinfo 
		temp_store 
		threads 
		trusted_schema 
		x	user_version 
		wal_autocheckpoint 
		wal_checkpoint 
	 @formatter:on */

	/**
	 * SQL String / get PRAGMA "application_id"
	 */
	public static final String SQL_GET_APPLICATION_ID = getPragma(PRAGMA_APPLICATION_ID);
	/**
	 * SQL String / get PRAGMA "schema_version"
	 */
	public static final String SQL_GET_SCHEMA_VERSION = getPragma(PRAGMA_SCHEMA_VERSION);
	/**
	 * SQL String / get PRAGMA "user_version"
	 */
	public static final String SQL_GET_USER_VERSION = getPragma(PRAGMA_USER_VERSION);
	/**
	 * SQL String / get PRAGMA "foreign_keys"
	 */
	public static final String SQL_GET_FOREIGN_KEYS = getPragma(PRAGMA_FOREIGN_KEYS);
	/**
	 * SQL String / execute PRAGMA "foreign_keys=1"
	 */
	public static final String SQL_ENABLE_FOREIGN_KEYS = setIntegerPragma(PRAGMA_FOREIGN_KEYS, 1);
	/**
	 * SQL String / execute PRAGMA "foreign_keys=0"
	 */
	public static final String SQL_DISABLE_FOREIGN_KEYS = setIntegerPragma(PRAGMA_FOREIGN_KEYS, 0);

	public final static String SQL_ENABLE_LEGACY_ALTER_TABLE = setStringPragma(PRAGMA_LEGACY_ALTER_TABLE, "ON");
	public final static String SQL_DISABLE_LEGACY_ALTER_TABLE = setStringPragma(PRAGMA_LEGACY_ALTER_TABLE, "OFF");

	/**
	 * get the SQL command to set the PRAGMA "application_id"
	 * 
	 * @param applicationId the id to set
	 * @return The SQL String to set the application id
	 */
	public static final String setApplicationId(int applicationId) {
		return String.format(SET_INTEGER_PRAGMA_FORMAT, PRAGMA_APPLICATION_ID, applicationId);
	}

	/**
	 * get the SQL command to update the PRAGMA "user_version"
	 * 
	 * @param version the "user_version" to set
	 * 
	 * @return the SQL update command to set the specified user version
	 */
	public static final String setUserVersion(long version) {
		return String.format(SET_INTEGER_PRAGMA_FORMAT, PRAGMA_USER_VERSION, version);
	}

	/**
	 * get the SQL query command to query a tables "table_info"
	 * <p>
	 * <b>Attention!!! </b> Use with care! the SQL command produced by this method
	 * is vulnerable to SQL injection!
	 * 
	 * @param tableName the table to inspect
	 * 
	 * @return the SQL query command to list the specified tables "table_info"
	 */
	public static final String setTableInfo(String tableName) {
		return setStringPragma(PRAGMA_TABLE_INFO, tableName);
	}

	/**
	 * Get a specific PRAGMA
	 * <p>
	 * <b>Attention!!! </b> Use with care! the SQL command produced by this method
	 * is vulnerable to SQL injection!
	 * 
	 * @param pragma the PRAGMA to get
	 * 
	 * @return the SQLite query to get the specified PRAGMA
	 */
	public static final String getPragma(String pragma) {
		return String.format(GET_PRAGMA_FORMAT, pragma);
	}

	/**
	 * Set / execute a specific INTEGER PRAGMA
	 * <p>
	 * <b>Attention!!!</b> Use with care! the SQL command produced by this method is
	 * vulnerable to SQL injection!
	 * 
	 * @param pragma the PRAGMA to set / execute
	 * @param value  the value to use
	 * 
	 * @return the SQL command to set the specified PRAGMA to the supplied value.
	 */
	public static final String setIntegerPragma(String pragma, long value) {
		return String.format(SET_INTEGER_PRAGMA_FORMAT, pragma, value);
	}

	/**
	 * Set / execute a specific TEXT PRAGMA
	 * <p>
	 * <b>Attention!!!</b> Use with care! the SQL command produced by this method is
	 * vulnerable to SQL injection!
	 * 
	 * @param pragma the PRAGMA to set
	 * @param value  the value to use
	 * 
	 * @return the SQL command to set the specified PRAGMA to the supplied value.
	 */
	public static final String setStringPragma(String pragma, String value) {
		return String.format(SET_STRING_PRAGMA_FORMAT, pragma, value);
	}

	/**
	 * Set specific hex pragma
	 * <p>
	 * <b>Attention!!!</b> Use with care! the SQL command produced by this method is
	 * vulnerable to SQL injection!
	 * 
	 * @param pragma the pragma to set
	 * @param value  the hex value string
	 * 
	 * @return the SQL command to execute the PRAGMA statement
	 */
	public static final String setHexPragma(String pragma, String value) {
		return String.format(SET_HEX_PRAGMA_FORMAT, pragma, value);
	}
} // end of class Pragma
