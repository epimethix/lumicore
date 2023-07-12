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
package com.github.epimethix.lumicore.sqlite;

/* 
 * ChatGPT Playground input:
 * create a java enum class that contains all SQLite Keywords as enum constants, 
 * implement a public static method named isKeyword(String word) that tests if the 
 * string word is a valid enum constant value of this class. 
 */

/*

public enum SQLiteKeywords {
	ABORT, ACTION, ADD, AFTER, ALL, ALTER, ANALYZE, AND, AS, ASC, ATTACH, AUTOINCREMENT, BEFORE, BEGIN, BETWEEN, BY,
	CASCADE, CASE, CAST, CHECK, COLLATE, COLUMN, COMMIT, CONFLICT, CONSTRAINT, CREATE, CROSS, CURRENT_DATE,
	CURRENT_TIME, CURRENT_TIMESTAMP, DATABASE, DEFAULT, DEFERRABLE, DEFERRED, DELETE, DESC, DETACH, DISTINCT, DROP,
	EACH, ELSE, END, ESCAPE, EXCEPT, EXCLUSIVE, EXISTS, EXPLAIN, FAIL, FILTER, FOLLOWING, FOR, FOREIGN, FROM, FULL,
	GLOB, GROUP, HAVING, IF, IGNORE, IMMEDIATE, IN, INDEX, INDEXED, INITIALLY, INNER, INSERT, INSTEAD, INTERSECT, INTO,
	IS, ISNULL, JOIN, KEY, LEFT, LIKE, LIMIT, MATCH, NATURAL, NO, NOT, NOTNULL, NULL, OF, OFFSET, ON, OR, ORDER, OUTER,
	PLAN, PRAGMA, PRIMARY, QUERY, RAISE, RECURSIVE, REFERENCES, REGEXP, REINDEX, RELEASE, RENAME, REPLACE, RESTRICT,
	RIGHT, ROLLBACK, ROW, SAVEPOINT, SELECT, SET, TABLE, TEMP, TEMPORARY, THEN, TO, TRANSACTION, TRIGGER, UNBOUNDED,
	UNION, UNIQUE, UPDATE, USING, VACUUM, VALUES, VIEW, VIRTUAL, WHEN, WHERE;

	public static boolean isKeyword(String word) {
		for (SQLiteKeywords key : SQLiteKeywords.values()) {
			if (key.name().equalsIgnoreCase(word)) {
				return true;
			}
		}
		return false;
	}
}
*/

/* ChatGPT Playground input:
 * create a java enum class that contains all SQLite Keywords as enum constants, 
 * implement a public static method named isKeyword(String word) that tests if the 
 * string word is a valid enum constant value of this class using the 
 * Enum.valueOf(String) method. 
 */

public enum SQLiteKeywords {
	ABORT, ACTION, ADD, AFTER, ALL, ALTER, ANALYZE, AND, AS, ASC, ATTACH, AUTOINCREMENT, BEFORE, BEGIN, BETWEEN, BY,
	CASCADE, CASE, CAST, CHECK, COLLATE, COLUMN, COMMIT, CONFLICT, CONSTRAINT, CREATE, CROSS, CURRENT_DATE,
	CURRENT_TIME, CURRENT_TIMESTAMP, DATABASE, DEFAULT, DEFERRABLE, DEFERRED, DELETE, DESC, DETACH, DISTINCT, DROP,
	EACH, ELSE, END, ESCAPE, EXCEPT, EXCLUSIVE, EXISTS, EXPLAIN, FAIL, FOR, FOREIGN, FROM, FULL, GLOB, GROUP, HAVING,
	IF, IGNORE, IMMEDIATE, IN, INDEX, INDEXED, INITIALLY, INNER, INSERT, INSTEAD, INTERSECT, INTO, IS, ISNULL, JOIN,
	KEY, LEFT, LIKE, LIMIT, MATCH, NATURAL, NO, NOT, NOTNULL, NULL, OF, OFFSET, ON, OR, ORDER, OUTER, PLAN, PRAGMA,
	PRIMARY, QUERY, RAISE, RECURSIVE, REFERENCES, REGEXP, REINDEX, RELEASE, RENAME, REPLACE, RESTRICT, RIGHT, ROLLBACK,
	ROW, SAVEPOINT, SELECT, SET, TABLE, TEMP, TEMPORARY, THEN, TO, TRANSACTION, TRIGGER, UNION, UNIQUE, UPDATE, USING,
	VACUUM, VALUES, VIEW, VIRTUAL, WHEN, WHERE;

	public static boolean isKeyword(String word) {
		try {
			SQLiteKeywords.valueOf(word);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}