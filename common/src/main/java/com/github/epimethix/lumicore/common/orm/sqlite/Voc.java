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
package com.github.epimethix.lumicore.common.orm.sqlite;

/**
 * Vocabulary constants
 */
public final class Voc {
	/**
	 * the String "INTEGER"
	 */
	public final static String INTEGER = "INTEGER";
	/**
	 * the String "INTEGER PRIMARY KEY"
	 */
	public final static String INTEGER_PK = "INTEGER PRIMARY KEY";
	/**
	 * the String "INTEGER PRIMARY KEY AUTOINCREMENT"
	 */
	public final static String INTEGER_PK_AI = "INTEGER PRIMARY KEY AUTOINCREMENT";
	/**
	 * the String "REAL"
	 */
	public final static String REAL = "REAL";
	/**
	 * the String "REAL PRIMARY KEY"
	 */
	public final static String REAL_PK = "REAL PRIMARY KEY";
	/**
	 * the String "TEXT"
	 */
	public final static String TEXT = "TEXT";
	/**
	 * the String "TEXT PRIMARY KEY"
	 */
	public final static String TEXT_PK = "TEXT PRIMARY KEY";
	/**
	 * the String "BLOB"
	 */
	public final static String BLOB = "BLOB";
	/**
	 * the String "WITHOUT ROWID"
	 */
	public final static String WITHOUT_ROWID = "WITHOUT ROWID";
	/**
	 * the String "NOT NULL"
	 */
	public static final String NOT_NULL = "NOT NULL";
	/**
	 * the String "DEFAULT"
	 */
	public static final String DEFAULT = "DEFAULT";
	/**
	 * the String "DEFAULT NULL"
	 */
	public static final String DEFAULT_NULL = "DEFAULT NULL";
	/**
	 * the String "PRIMARY KEY"
	 */
	public final static String PRIMARY_KEY = "PRIMARY KEY";
	/**
	 * the String "FOREIGN KEY"
	 */
	public final static String FOREIGN_KEY = "FOREIGN KEY";
	/**
	 * the String "REFERENCES"
	 */
	public final static String FK_REFERENCES = "REFERENCES";
	/**
	 * the String "UNIQUE"
	 */
	public final static String UNIQUE = "UNIQUE";
	/**
	 * the String "CHECK"
	 */
	public final static String CHECK = "CHECK";

	/**
	 * All constraint names
	 */
	// public final static String[] CONSTRAINT_NAMES = new String[] { PRIMARY_KEY,
	// FOREIGN_KEY, UNIQUE, CHECK };
}