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
package com.github.epimethix.lumicore.orm.sqlite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.sqlite.Constraint;
import com.github.epimethix.lumicore.common.orm.sqlite.Definition;
import com.github.epimethix.lumicore.orm.query.Order;

/**
 * Builds SQLite statements in an underlying StringBuilder. Returns the
 * generated SQLite statement through the getSQL() or toString() methods. All
 * names are put in back ticks to avoid name collisions with SQL keywords.
 * 
 * @author epimethix
 *
 */
@Deprecated
public final class SQLiteBuilder {


	/**
	 * Creates a new SQLBuilder
	 * 
	 * @return a new SQLBuilder
	 */
	public final static SQLiteBuilder newBuilder() {
		return new SQLiteBuilder();
	}

	/**
	 * Creates a new SQLBuilder containing the specified initial sql string
	 * 
	 * @return a new SQLBuilder containing the specified initial sql string
	 */
	public final static SQLiteBuilder newBuilder(SQLiteBuilder initialSQL) {
		return new SQLiteBuilder(initialSQL);
	}

	/*
	 * 
	 * 
	 * Object
	 * 
	 * 
	 */

	/**
	 * StringBuilder containing the sql query string being built.
	 */
	private final StringBuilder sql;

	private int tableCounter;

	private final List<String[]> aliases;

	private final List<Object> values;

	/**
	 * create a new, empty SQLBuilder.
	 * 
	 * @see SQLiteBuilder#newBuilder()
	 */
	public SQLiteBuilder() {
		this(null);
	}

	/**
	 * create a new SQLBuilder containing the specified initial SQL String.
	 * 
	 * @param initialSQL non null/String containing the beginning of the query.
	 * @see #newBuilder(SQLiteBuilder)
	 */
	public SQLiteBuilder(SQLiteBuilder initialSQL) {
		if (Objects.nonNull(initialSQL)) {
			this.sql = new StringBuilder(initialSQL.toString().trim() + " ");
			this.aliases = new ArrayList<>(initialSQL.aliases);
			this.tableCounter = initialSQL.tableCounter;
			this.values = new ArrayList<>(initialSQL.values);
//			for(String[] alias:aliases)
//			System.out.println("==========" + Arrays.toString(alias));
		} else {
			sql = new StringBuilder();
			aliases = new ArrayList<>();
			values = new ArrayList<>();
			tableCounter = 1;
		}
//		System.out.println("-------------------------");
	}

	private boolean isUpdate() {
		if (sql.length() > 6)
			return sql.substring(0, 6).toUpperCase().equals("UPDATE");
		else
			return false;
	}

	private String generateAlias(String entity) {
		String alias = String.format("t%d", this.tableCounter++);
		aliases.add(new String[] { entity, alias });
		return alias;
	}

	private String getAlias(String entity) {
		return getAlias(entity, 0);
	}

	private String getAlias(String entity, int offset) {
		String alias = null;
		for (int i = 0; i < aliases.size(); i++) {
			if (aliases.get(i)[0].equals(entity)) {
				if (offset == 0) {
					alias = aliases.get(i)[1];
					break;
				} else {
					offset--;
				}
			}
		}
//		if (Objects.isNull(alias)) {
//			alias = generateAlias(entity);
//			System.out.println("~~~NULL~~~~  # " + entity);
//		} else {
//			System.out.println("+~~~"+alias+"~~~~+  # " + entity);
//		}
		return alias;
	}

	/**
	 * puts a name between back ticks and appends a space (`name` )
	 * 
	 * @param name the name to surround
	 * 
	 * @return the safe name followed by space
	 */
//	private final static String backtick(String name) {
//		return "`".concat(name).concat("` ");
//	}
//
//	private final static String backtick(String entity, String field) {
//		return String.format("`%s`.`%s` ", entity, field);
//	}

	private final SQLiteBuilder appendAliasField(String alias, String field) {
		sql.append(alias).append(".`").append(field).append("` ");
		return this;
	}

	private final SQLiteBuilder appendEntityField(String entity, String field) {
		sql.append("`").append(entity).append("`.`").append(field).append("` ");
		return this;
	}

	public final SQLiteBuilder values(Object... values) {
		this.values.addAll(Arrays.asList(values));
		return this;
	}

	public final List<Object> getValues() {
		return Collections.unmodifiableList(values);
	}

	/**
	 * CREATE TABLE IF NOT EXISTS
	 * 
	 * @param entity the table name to be created
	 * @param fields the field Definitions
	 * 
	 * @return this builder
	 * 
	 * @see Definition#define(String, int)
	 * @see Definition#define(String, int, int)
	 * @see Definition#define(String, int, int, int)
	 * @see Definition#define(String, int, int, int, Object)
	 */
	public SQLiteBuilder createTableIfNotExists(String entity, Definition... fields) {
		return create(true, entity, fields);
	}

	/**
	 * CREATE TABLE
	 * 
	 * @param entity the table name to be created
	 * @param fields the field Definitions
	 * 
	 * @return this builder
	 * 
	 * @see Definition#define(String, int)
	 * @see Definition#define(String, int, int)
	 * @see Definition#define(String, int, int, int)
	 * @see Definition#define(String, int, int, int, Object)
	 */
	public SQLiteBuilder createTable(String entity, Definition... fields) {
		return create(false, entity, fields);
	}

	/**
	 * CREATE TABLE
	 * 
	 * @param ifNotExists IF NOT EXISTS
	 * @param entity      the table name to be created
	 * @param fields      the field Definitions
	 * 
	 * @return this builder
	 * 
	 * @see Definition#define(String, int)
	 * @see Definition#define(String, int, int)
	 * @see Definition#define(String, int, int, int)
	 * @see Definition#define(String, int, int, int, Object)
	 */
	private SQLiteBuilder create(boolean ifNotExists, String entity, Definition... fields) {
		if (ifNotExists) {
			sql.append("CREATE TABLE IF NOT EXISTS ");
		} else {
			sql.append("CREATE TABLE ");
		}
		sql.append("`").append(entity).append("` ( ");
		for (int i = 0; i < fields.length; i++) {
			sql.append(fields[i].toString());
			if (i + 1 < fields.length) {
				sql.append(", ");
			} else {
				sql.append(") ");
			}
		}
		return this;
	}

	/**
	 * Add constraints, use after {@link #createTable(String, Definition...)} or
	 * {@link #createTableIfNotExists(String, Definition...)}
	 * 
	 * @param constraints the constraints
	 * 
	 * @return this builder
	 * 
	 * @see Constraint#foreignKeyConstraint(String, String, String)
	 * @see Constraint#primaryKeyConstraint(String...)
	 * @see Constraint#uniqueConstraint(String...)
	 * @see Constraint#checkConstraint(String)
	 */
	public SQLiteBuilder withConstraints(Constraint... constraints) {
		int x = sql.lastIndexOf(")");
		if (x > 0 && constraints.length > 0) {
			StringBuilder sql2 = new StringBuilder();
			sql2.append(", ");
			for (int i = 0; i < constraints.length; i++) {
				sql2.append(constraints[i].getSQL());
				if (i + 1 < constraints.length) {
					sql2.append(", ");
				}
			}
			sql.insert(x, sql2.toString());
		}
		return this;
	}

	/**
	 * Append WITHOUT ROWID after create statement
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder withoutRowId() {
		sql.append("WITHOUT ROWID ");
		return this;
	}

	/**
	 * begin with SELECT statement.
	 * 
	 * @param entity the entity name to select
	 * @param fields the fields to select
	 * 
	 * @return this builder
	 * 
	 * @see #joinOnSelect(String, String, String, String, String...)
	 * @see #joinOnSelectLeft(String, String, String, String, String...)
	 * @see #joinSelect2(String, String, String, String, String, String, String...)
	 * @see #joinSelect2(boolean, String, String, String, String, String, String,
	 *      String...)
	 * 
	 * @see #where(String, String...)
	 */
	public SQLiteBuilder selectFrom(String entity, String... fields) {
		String initialSQL = sql.toString().toUpperCase(Locale.ENGLISH);
		boolean insert = initialSQL.startsWith("INSERT INTO");
		if (insert) {
			int indexOfValues = initialSQL.indexOf("VALUES");
			if (indexOfValues > -1) {
				sql.delete(indexOfValues, sql.length());
			}
		}
		sql.append("SELECT ");
		String alias = generateAlias(entity);
		for (int i = 0; i < fields.length; i++) {
//			if(insert) {
////				appendEntityField(entity, fields[i]);
//				sql.append("`").append(entity).append("`");
//			} else {
////				appendAliasField(alias, fields[i]);
//			}
			sql.append(alias);
			sql.append(".`").append(fields[i]).append("`");
			if (i + 1 < fields.length) {
				sql.append(", ");
			} else {
				sql.append(" ");
			}
		}
		sql.append("FROM `").append(entity);
		sql.append("` ").append(alias);
		sql.append(" ");
		return this;
	}

	/**
	 * append (INNER) JOIN after SELECT or JOIN and fields from joined entity
	 * 
	 * @param entity         the entity containing the FOREIGN KEY
	 * @param key            the PRIMARY KEY field
	 * @param entityJoin     the entity to be JOINed
	 * @param foreignKey     the FOREIGN KEY field
	 * @param fieldsToSelect the fields to append to the select statement
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder joinOnSelect(String entity, String key, String entityJoin, String foreignKey,
			String... fieldsToSelect) {
		return joinSelect(false, entity, key, entityJoin, foreignKey, fieldsToSelect);
	}

	/**
	 * append LEFT JOIN after SELECT or JOIN and fields from joined entity
	 * 
	 * @param entity         the entity containing the FOREIGN KEY
	 * @param key            the PRIMARY KEY field
	 * @param entityJoin     the entity to be LEFT JOINed
	 * @param foreignKey     the FOREIGN KEY field
	 * @param fieldsToSelect the fields to append to the select statement
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder joinOnSelectLeft(String entity, String key, String entityJoin, String foreignKey,
			String... fieldsToSelect) {
		return joinSelect(true, entity, key, entityJoin, foreignKey, fieldsToSelect);
	}

	/**
	 * @param left           true for LEFT, false for default (INNER) JOIN
	 * 
	 * @param entity         the entity containing the FOREIGN KEY
	 * @param key            the PRIMARY KEY field
	 * @param entityJoin     the entity to be LEFT JOINed
	 * @param foreignKey     the FOREIGN KEY field
	 * @param fieldsToSelect the fields to append to the select statement
	 * 
	 * @return this builder
	 */
	private SQLiteBuilder joinSelect(boolean left, String entity, String key, String entityJoin, String foreignKey,
			String... fieldsToSelect) {
		if (sql.indexOf(" FROM") > -1) {
			if (sql.charAt(sql.length() - 1) != ' ') {
				sql.append(" ");
			}
			String entityAlias = getAlias(entity);
//			if(entityAlias == null) {
//				System.out.println("null");
//			}
			String entityJoinAlias = generateAlias(entityJoin);
			// @formatter:off
			sql.append(String.format("%sJOIN `%s` %s ON %s.`%s`=%s.`%s` ", 
							left ? "LEFT " : "", 
							entityJoin, 
							entityJoinAlias, 
							entityAlias,
							key, 
							entityJoinAlias, 
							foreignKey));
			// @formatter:on
			for (String f : fieldsToSelect) {
				sql.insert(sql.indexOf(" FROM"), String.format(", %s.`%s`", entityJoinAlias, f));
			}
		}
		return this;
	}

	/**
	 * 
	 * 
	 * @param entity
	 * @param key
	 * @param key2
	 * @param entityJoin
	 * @param foreignKey
	 * @param foreignKey2
	 * @param fieldsToSelect
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder joinSelect2(String entity, String key, String key2, String entityJoin, String foreignKey,
			String foreignKey2, String... fieldsToSelect) {
		return joinSelect2(false, entity, key, key2, entityJoin, foreignKey, foreignKey2, fieldsToSelect);
	}

	public SQLiteBuilder joinSelectLeft2(String entity, String key, String key2, String entityJoin, String foreignKey,
			String foreignKey2, String... fieldsToSelect) {
		return joinSelect2(true, entity, key, key2, entityJoin, foreignKey, foreignKey2, fieldsToSelect);
	}

	/**
	 * 
	 * @param left           true for left join
	 * @param entity
	 * @param key
	 * @param key2
	 * @param entityJoin
	 * @param foreignKey
	 * @param foreignKey2
	 * @param fieldsToSelect
	 * @return this builder
	 */
	public SQLiteBuilder joinSelect2(boolean left, String entity, String key, String key2, String entityJoin,
			String foreignKey, String foreignKey2, String... fieldsToSelect) {
		if (sql.indexOf(" FROM") > -1) {
			if (sql.charAt(sql.length() - 1) != ' ') {
				sql.append(" ");
			}
			String entityAlias = getAlias(entity);
			sql.append(String.format("%sJOIN `%s` %s ON `%s`.`%s`=`%s`.`%s` AND `%s`.`%s`=`%s`.`%s` ",
					left ? "LEFT " : "", entityJoin, entityAlias, entity, key, entityJoin, foreignKey, entity, key2,
					entityJoin, foreignKey2));
			for (String f : fieldsToSelect) {
				sql.insert(sql.indexOf(" FROM"), String.format(", %s.`%s`", entityAlias, f));
			}
		}
		return this;
	}

	private final void where(boolean openBracket, boolean or, String entity, String... fields) {
		sql.append("WHERE ");
		if (openBracket) {
			this.openBracket();
		}
		String alias = null;
		if (!isUpdate()) {
			alias = getAlias(entity);
		}
		if (fields.length == 1) {
			if (Objects.nonNull(alias)) {
				appendAliasField(alias, fields[0]);
			} else {
				appendEntityField(entity, fields[0]);
			}
			sql.append("= ? ");
		} else {
			for (int i = 0; i < fields.length; i++) {
				if (i == 0) {
					if (Objects.nonNull(alias)) {
						appendAliasField(alias, fields[i]);
					} else {
						appendEntityField(entity, fields[i]);
					}
					sql.append("= ? ");
				} else {
					if (or) {
						or(entity, fields[i]);
					} else {
						and(entity, fields[i]);
					}
				}
			}
		}
	}

	/**
	 * append WHERE field == 0
	 * 
	 * @param field the field to filter by
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder whereIsZero(String entity, String field) {
		sql.append("WHERE ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("= 0 ");
		return this;
	}

	/**
	 * append WHERE field == 0
	 * 
	 * @param field the field to filter by
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder whereIsNull(String entity, String field) {
		sql.append("WHERE ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("IS NULL ");
		return this;
	}

	/**
	 * append WHERE field == 0
	 * 
	 * @param field the field to filter by
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder whereIsNotNull(String entity, String field) {
		sql.append("WHERE ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("IS NOT NULL ");
		return this;
	}

	/**
	 * append AND field == 0
	 * 
	 * @param field the field to filter by
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder andIsZero(String entity, String field) {
		sql.append("AND ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("== 0 ");
		return this;
	}

	/**
	 * Append WHERE clause after {@link #selectFrom(String, String...)}.
	 * <p>
	 * This method is a synonym for
	 * {@link SQLiteBuilder#whereAnd(String, String...)}
	 * 
	 * @param fields one or more fields (AND)
	 * 
	 * @return this builder
	 * 
	 * @see #and(String, String)
	 * @see #andIsNull(String, String)
	 * @see #or(String, String)
	 * @see #orIsNull(String, String)
	 * @see #openBracket()
	 * @see #closeBracket()
	 * @see #orderBy(String, String...)
	 * @see #orderBy(Order, String, String...)
	 */
	public SQLiteBuilder where(String entity, String... fields) {
		return whereAnd(entity, fields);
	}

	/**
	 * append WHERE clause
	 * 
	 * @param fields one or more fields (AND)
	 * 
	 * @return this builder
	 * 
	 */
	public SQLiteBuilder whereAnd(String entity, String... fields) {
		where(false, false, entity, fields);
		return this;
	}

	public SQLiteBuilder whereGreaterThan(String entity, String field) {
		sql.append("WHERE ");
		String alias = null;
		if (!isUpdate()) {
			alias = getAlias(entity);
		}
		if (Objects.nonNull(alias)) {
			appendAliasField(alias, field);
		} else {
			appendEntityField(entity, field);
		}
		sql.append("> ? ");
		return this;
	}

	public SQLiteBuilder whereLessThan(String entity, String field) {
		sql.append("WHERE ");
		String alias = null;
		if (!isUpdate()) {
			alias = getAlias(entity);
		}
		if (Objects.nonNull(alias)) {
			appendAliasField(alias, field);
		} else {
			appendEntityField(entity, field);
		}
		sql.append("< ? ");
		return this;
	}

	public SQLiteBuilder whereLike(String entity, String field) {
		sql.append("WHERE ");
		String alias = null;
		if (!isUpdate()) {
			alias = getAlias(entity);
		}
		if (Objects.nonNull(alias)) {
			appendAliasField(alias, field);
		} else {
			appendEntityField(entity, field);
		}
		sql.append("LIKE ? ");
		return this;
	}

	/**
	 * append WHERE clause
	 * 
	 * @param fields one or more fields (OR)
	 * 
	 * @return this builder
	 * 
	 */
	public SQLiteBuilder whereOr(String entity, String... fields) {
		where(false, true, entity, fields);
		return this;
	}

	/**
	 * append WHERE clause and open bracket before the field(s)
	 * 
	 * @param fields one or more fields (AND)
	 * 
	 * @return this builder
	 * 
	 */
	public SQLiteBuilder whereAndOpenBracket(String entity, String... fields) {
		where(true, false, entity, fields);
		return this;
	}

	/**
	 * append WHERE clause and open bracket before the field(s)
	 * 
	 * @param fields one or more fields (or)
	 * 
	 * @return this builder
	 * 
	 */
	public SQLiteBuilder whereOrOpenBracket(String entity, String... fields) {
		where(true, true, entity, fields);
		return this;
	}

	/**
	 * append: AND field = ?
	 * 
	 * @param field the field
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder and(String entity, String field) {
		sql.append("AND ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("=? ");
		return this;
	}

	public SQLiteBuilder andGreaterThan(String entity, String field) {
		sql.append("AND ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("> ? ");
		return this;
	}

	public SQLiteBuilder andLessThan(String entity, String field) {
		sql.append("AND ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("< ? ");
		return this;
	}

	/**
	 * append: AND field LIKE ?
	 * 
	 * @param field the field
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder andLike(String entity, String field) {
		sql.append("AND ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("LIKE ? ");
		return this;
	}

	/**
	 * append: AND field NOT LIKE ?
	 * 
	 * @param field the field
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder andNotLike(String entity, String field) {
		sql.append("AND ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("NOT LIKE ? ");
		return this;
	}

	/**
	 * append: AND field IS NULL
	 * 
	 * @param field the field
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder andIsNull(String entity, String field) {
		sql.append("AND ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("IS NULL ");
		return this;
	}

	/**
	 * append: AND field IS NOT NULL
	 * 
	 * @param field the field
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder andIsNotNull(String entity, String field) {
		sql.append("AND ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("IS NOT NULL ");
		return this;
	}

	/**
	 * append: OR field = ?
	 * 
	 * @param field the field
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder or(String entity, String field) {
		sql.append("OR ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("=? ");
		return this;
	}

	/**
	 * append: OR field = ?
	 * 
	 * @param field the field
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder orLike(String entity, String field) {
		sql.append("OR ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("LIKE ? ");
		return this;
	}

	/**
	 * append: OR field IS NULL
	 * 
	 * @param field the field
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder orIsNull(String entity, String field) {
		sql.append("OR ");
		if (isUpdate()) {
			appendEntityField(entity, field);
		} else {
			appendAliasField(getAlias(entity), field);
		}
		sql.append("IS NULL ");
		return this;
	}

	/**
	 * append open bracket
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder openBracket() {
		sql.append("( ");
		return this;
	}

	/**
	 * append close bracket
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder closeBracket() {
		sql.append(") ");
		return this;
	}

	public SQLiteBuilder groupBy(String entity, String field, String... more) {
		String alias = null;
		if (!isUpdate()) {
			alias = getAlias(entity);
		}
		sql.append("GROUP BY ");
		if (Objects.nonNull(alias)) {
			appendAliasField(alias, field);
		} else {
			appendEntityField(entity, field);
		}
		for(int i = 0; i < more.length;i++) {
			sql.append(", ");
			if (Objects.nonNull(alias)) {
				appendAliasField(alias, more[i]);
			} else {
				appendEntityField(entity, more[i]);
			}
		}
		return this;
	}

	public SQLiteBuilder having(String entity, String... fields) {
		return having(false, false, entity, fields);
	}

	private SQLiteBuilder having(boolean or, boolean openBracket, String entity, String... fields) {
		sql.append("HAVING ");
		if (openBracket) {
			this.openBracket();
		}
		String alias = null;
		if (!isUpdate()) {
			alias = getAlias(entity);
		}
		if (fields.length == 1) {
			if (Objects.nonNull(alias)) {
				appendAliasField(alias, fields[0]);
			} else {
				appendEntityField(entity, fields[0]);
			}
			sql.append("= ? ");
		} else {
			for (int i = 0; i < fields.length; i++) {
				if (i == 0) {
					if (Objects.nonNull(alias)) {
						appendAliasField(alias, fields[i]);
					} else {
						appendEntityField(entity, fields[i]);
					}
					sql.append("= ? ");
				} else {
					if (or) {
						or(entity, fields[i]);
					} else {
						and(entity, fields[i]);
					}
				}
			}
		}
		return this;
	}

	public SQLiteBuilder havingGreaterThan(String entity, String field) {
		String alias = getAlias(entity);
		sql.append("HAVING ").append(alias).append(".`").append(field).append("` > ? ");
		return this;
	}

	/**
	 * append order by the specified field(s) ascending
	 * 
	 * @param fields the fields to order by
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder orderBy(String entity, String... fields) {
		return orderBy(Order.ASC, entity, fields);
	}

	/**
	 * append order by the specified field(s) descending
	 * 
	 * @param fields the fields to order by
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder orderByDesc(String entity, String... fields) {
		return orderBy(Order.DESC, entity, fields);
	}

	/**
	 * append order by the specified fields in the specified direction
	 * 
	 * @param order  the direction: either Order.ASC or Order.DESC
	 * @param fields the field(s) to order by
	 * 
	 * @return this builder
	 * 
	 * @see Order#ASC
	 * @see Order#DESC
	 */
	private SQLiteBuilder orderBy(Order order, String entity, String... fields) {
		sql.append("ORDER BY ");
		String alias = getAlias(entity);
		for (int i = 0; i < fields.length; i++) {
			sql.append(alias).append(".`").append(fields[i]).append("`");
			if (i + 1 < fields.length) {
				sql.append(", ");
			} else {
				sql.append(" ");
				if (order == Order.DESC) {
					sql.append("DESC ");
				}
			}
		}
		return this;
	}

	public SQLiteBuilder limit(int limit) {
		sql.append("LIMIT ").append(String.valueOf(limit)).append(" ");
		return this;
	}

	public SQLiteBuilder limit(int limit, long offset) {
		sql.append("LIMIT ").append(String.valueOf(limit)).append(" OFFSET ").append(String.valueOf(offset))
				.append(" ");
		return this;
	}

	/**
	 * append INSERT INTO statement
	 * 
	 * @param entity the entity to insert into
	 * @param fields the fields to fill
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder insertInto(String entity, String... fields) {
		sql.append("INSERT INTO `").append(entity).append("` (");
		for (int i = 0; i < fields.length; i++) {
			sql.append("`").append(fields[i]).append("`");
			if (i + 1 < fields.length) {
				sql.append(", ");
			} else {
				sql.append(") VALUES (");
			}
		}
		for (int i = 0; i < fields.length; i++) {
			sql.append("?");
			if (i + 1 < fields.length) {
				sql.append(", ");
			} else {
				sql.append(")");
			}
		}
		return this;
	}

	/**
	 * append UPDATE / SET statement
	 * <p>
	 * <b>Attention!!!</b> don't forget to append the WHERE clause or this statement
	 * will overwrite all records in the table!!!
	 * 
	 * @param entity the entity to update
	 * @param fields the fields to set
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder updateSet(String entity, String... fields) {
		sql.append("UPDATE `").append(entity).append("` SET ");
		for (int i = 0; i < fields.length; i++) {
			sql.append("`").append(fields[i]).append("`=?");
			if (i + 1 < fields.length) {
				sql.append(", ");
			} else {
				sql.append(" ");
			}
		}
		return this;
	}

	/**
	 * append DELETE FROM / WHERE statement
	 * 
	 * @param entity the entity name to delete from
	 * @param fields the field(s) to select the deletion record(s) by
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder deleteFromWhere(String entity, String... fields) {
		String alias = generateAlias(entity);
		sql.append("DELETE FROM `").append(entity).append("` ").append(alias).append(" ");
		where(entity, fields);
		return this;
	}

	/**
	 * append DROP TABLE statement
	 * 
	 * @param tableName the table to drop
	 * 
	 * @return this builder
	 */
	public SQLiteBuilder dropTable(String tableName) {
		sql.append("DROP TABLE `").append(tableName).append("`");
		return this;
	}

	/**
	 * @return the current SQL String
	 */
	public String getSQL() {
		return sql.toString().trim();
	}

	/**
	 * get the current SQL String
	 */
	@Override
	public String toString() {
		return getSQL();
	}
}
