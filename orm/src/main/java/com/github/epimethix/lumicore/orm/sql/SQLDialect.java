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
package com.github.epimethix.lumicore.orm.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.query.Query;
import com.github.epimethix.lumicore.common.orm.query.Query.CreateBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.CreateIndexBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.CreateIndexQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.CriteriaBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.DeleteBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.DeleteQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.InsertBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.UpdateBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.UpdateQuery;
import com.github.epimethix.lumicore.common.orm.sql.ConnectionFactory;
import com.github.epimethix.lumicore.common.orm.sql.Dialect;
import com.github.epimethix.lumicore.common.orm.sqlite.Constraint;
import com.github.epimethix.lumicore.common.orm.sqlite.Definition;
import com.github.epimethix.lumicore.orm.query.SQLCreateBuilder;
import com.github.epimethix.lumicore.orm.query.SQLCreateIndexBuilder;
import com.github.epimethix.lumicore.orm.query.SQLDeleteBuilder;
import com.github.epimethix.lumicore.orm.query.SQLInsertBuilder;
import com.github.epimethix.lumicore.orm.query.SQLSelectBuilder;
import com.github.epimethix.lumicore.orm.query.SQLUpdateBuilder;

public abstract class SQLDialect implements Dialect {

	protected final Database DB;

	protected final ConnectionFactory CONNECTION_FACTORY;

	private Connection connection;

	private final String quotationChar;

	public SQLDialect(Database db, ConnectionFactory connectionFactory) {
		this.DB = db;
		this.CONNECTION_FACTORY = connectionFactory;
		String qc = " ";
		try {
			Connection c = getConnection();
			qc = c.getMetaData().getIdentifierQuoteString();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.quotationChar = qc;
	}
	
	/*
	 * * * ConnectionController
	 */

	@Override
	public boolean isConnectionWorking() {
		try {
			CONNECTION_FACTORY.testConnection();
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isDeployed() {
		return CONNECTION_FACTORY.isDeployed();
	}

	@Override
	public void close() throws SQLException {
		if (Objects.nonNull(connection)) {
			connection.close();
			connection = null;
		}
	}

	@Override
	public Connection createConnection() throws SQLException {
		return CONNECTION_FACTORY.createConnection();
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (Objects.isNull(connection) || connection.isClosed()) {
			connection = CONNECTION_FACTORY.createConnection();
		}
		return connection;
	}

	@Override
	public String getPath() {
		return CONNECTION_FACTORY.getConnectionString();
	}

	@Override
	public void checkClose(boolean closeConnection) throws SQLException {
		if (closeConnection) {
			close();
		}
	}
	
//	private static String getFieldName(String schemaName, Class<? extends Entity<?>> e, String field) {
//		return String.format("`%s`.`%s`.`%s`", schemaName, ORM.getEntityName(e), field);
//	}

//	private static String getFieldName(String alias, String field) {
//		return String.format("%s.`%s`", alias, field);
//	}

//	public static final CreateBuilder create(Class<? extends Entity<?>> e, Definition... definitions) {
//		return new SQLiteCreateBuilder(e, definitions);
//	}

	/*
	 * QueryBuilderFactory
	 */

	@Override
	public CreateBuilder create(String schemaName, Class<? extends Entity<?>> e, Definition... definitions) {
		return new SQLCreateBuilder(this, schemaName, e, definitions);
	}

	@Override
	public final CreateIndexBuilder createIndex(String schemaName, Class<? extends Entity<?>> e, String indexName,
			String... fields) {
		return new SQLCreateIndexBuilder(this, schemaName, e, indexName, fields);
	}

	@Override
	public final InsertBuilder insert(String schemaName, Class<? extends Entity<?>> e, String... fields) {
		return new SQLInsertBuilder(this, schemaName, e, fields);
	}

	@Override
	public final SelectBuilder select(String schemaName, Class<? extends Entity<?>> e, String... selection) {
		return new SQLSelectBuilder(this, schemaName, e, selection);
	}

	@Override
	public final UpdateBuilder update(String schemaName, Class<? extends Entity<?>> e) {
		return new SQLUpdateBuilder(this, schemaName, e);
	}

	@Override
	public final DeleteBuilder delete(String schemaName, Class<? extends Entity<?>> e) {
		return new SQLDeleteBuilder(this, schemaName, e);
	}

	/*
	 * * * QueryCompiler
	 */
	@Override
	public String getQuotationChar() {
		return quotationChar;
	}

	@Override
	public String quoteIdentifier(String identifier) {
		String qc = getQuotationChar();
		if(" ".equals(qc) || Objects.isNull(qc)) {
			return identifier;
		}
		return String.format("%s%s%s", qc, identifier, qc);
	}


	@Deprecated
	public static String getTableName(String schemaName, Class<? extends Entity<?>> e) {
		return String.format("`%s`.`%s`", schemaName, Entity.getEntityName(e));
	}


	@Override
	public String compileCreate(boolean temp, boolean ifNotExists, String schemaName, String tableName,
			List<Definition> definitions, List<Constraint> constraints, boolean strict, boolean withoutRowid) {
		StringBuilder b = new StringBuilder("CREATE ");
		if (temp) {
			b.append("TEMP ");
		}
		b.append("TABLE ");
		if (ifNotExists) {
			b.append("IF NOT EXISTS ");
		}
		b.append(tableName).append(" ");
		if (definitions.size() == 0) {
			b.append("AS ");
		} else {
			b.append("(");
			boolean firstRound = true;
			for (Definition def : definitions) {
				if (firstRound) {
					firstRound = false;
				} else {
					b.append(", ");
				}
				b.append(def.toString());
			}
			for (Constraint c : constraints) {
				b.append(", ").append(c.getSQL());
			}
			b.append(") ");
			if (strict) {
				b.append("STRICT ");
			}
			if (withoutRowid) {
				b.append("WITHOUT ROWID ");
			}
		}
//		System.err.println(b.toString());
		return b.toString();
	}

	@Override
	public String compileCreateIndex(boolean unique, boolean ifNotExists, String indexName, String schemaName,
			Class<? extends Entity<?>> e, String[] fields, CriteriaBuilder<CreateIndexBuilder, CreateIndexQuery> criteriaBuilder) {
		StringBuilder b = new StringBuilder();
		b.append("CREATE ");
		if (unique) {
			b.append("UNIQUE ");
		}
		b.append("INDEX ");
		if (ifNotExists) {
			b.append("IF NOT EXISTS ");
		}
		b.append("`").append(indexName).append("` ");
		b.append("ON `").append(schemaName).append("`.`").append(Entity.getEntityName(e)).append("` AS T01 ");
		b.append("(");
		boolean firstRound = true;
		for (String field : fields) {
			if (firstRound) {
				firstRound = false;
			} else {
				b.append(", ");
			}
			b.append("`").append(field).append("`");
		}
		b.append(")");
		if (!criteriaBuilder.isEmpty()) {
			b.append(" WHERE ").append(criteriaBuilder.buildCriteria());
		}
		return b.toString();
	}

	@Override
	public String compileInsert(Object schemaName, Class<? extends Entity<?>> e, String[] fields,
			List<? extends Entity<?>> records) {
		StringBuilder b = new StringBuilder();
		b.append("INSERT INTO `").append(schemaName).append("`.`").append(Entity.getEntityName(e)).append("` (");
		boolean first = true;
		for (String field : fields) {
			if (first) {
				first = false;
			} else {
				b.append(", ");
			}
			b.append("`").append(field).append("`");
		}
		b.append(") ");
		if (!records.isEmpty()) {
			b.append("VALUES (");
			first = true;
			for (int i = 0; i < fields.length; i++) {
				if (first) {
					first = false;
				} else {
					b.append(", ");
				}
				b.append("?");
			}
			b.append(")");
		}

//		System.err.println(b.toString());
		return b.toString();
	}

	@Override
	public String compileSelect(Query prev, boolean distinct, StringBuilder selectionBuilder, String tableName,
			String alias, StringBuilder joinBuilder, StringBuilder groupByBuilder,
			CriteriaBuilder<SelectBuilder, SelectQuery> criteriaBuilder, StringBuilder orderByBuilder, String nulls, Long limit,
			Long defLimit, Long offset) {
		StringBuilder b = new StringBuilder();
		if (Objects.nonNull(prev)) {
			b.append(prev.getQueryString());
		}
		b.append("SELECT ");
		if (distinct) {
			b.append("DISTINCT ");
		}
		b.append(selectionBuilder);
		b.append(" FROM ").append(tableName).append(" AS ").append(alias).append(" ").append(joinBuilder);
		if (groupByBuilder.length() != 0) {
			b.append("GROUP BY ").append(groupByBuilder).append(" ");
			if (!criteriaBuilder.isEmpty()) {
				b.append("HAVING ").append(criteriaBuilder.buildCriteria()).append(" ");
			}
		} else if (!criteriaBuilder.isEmpty()) {
			b.append("WHERE ").append(criteriaBuilder.buildCriteria()).append(" ");
		}
		if (orderByBuilder.length() != 0) {
			b.append("ORDER BY ").append(orderByBuilder).append(" ");
			if (Objects.nonNull(nulls)) {
				b.append(nulls);
			}
		}
		if (Objects.nonNull(limit)) {
			b.append("LIMIT ").append(String.valueOf(limit)).append(" ");
		} else if (Objects.nonNull(defLimit)) {
			b.append("LIMIT ").append(String.valueOf(defLimit)).append(" ");
		}
		if (Objects.nonNull(offset)) {
			b.append("OFFSET ").append(String.valueOf(offset));
		}

		return b.toString();
	}

	@Override
	public String compileUpdate(String schemaName, Class<? extends Entity<?>> e, String[] fields,
			CriteriaBuilder<UpdateBuilder, UpdateQuery> criteriaBuilder) {
		StringBuilder b = new StringBuilder();
		b.append("UPDATE `").append(schemaName).append("`.`").append(Entity.getEntityName(e)).append("` AS T01 ");
		b.append("SET ");
		boolean first = true;
		for (String field : fields) {
			if (first) {
				first = false;
			} else {
				b.append(", ");
			}
			b.append("`").append(field).append("` = ?");
		}
		if (!criteriaBuilder.isEmpty()) {
			b.append(" WHERE ").append(criteriaBuilder.buildCriteria());
		}
//		System.err.println(b.toString());
		return b.toString();
	}

	@Override
	public String compileDelete(Object schemaName, Class<? extends Entity<?>> e,
			CriteriaBuilder<DeleteBuilder, DeleteQuery> criteriaBuilder) {
		StringBuilder b = new StringBuilder("DELETE FROM ");
		b.append("`").append(schemaName).append("`.`").append(Entity.getEntityName(e)).append("` AS T01");
		if (!criteriaBuilder.isEmpty()) {
			b.append(" WHERE ").append(criteriaBuilder.buildCriteria());
		}
		return b.toString();
	}
}
