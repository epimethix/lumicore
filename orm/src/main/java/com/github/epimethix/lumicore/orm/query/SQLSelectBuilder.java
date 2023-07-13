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
package com.github.epimethix.lumicore.orm.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.query.Query;
import com.github.epimethix.lumicore.common.orm.query.Query.CriteriaBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.QueryBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectQuery;
import com.github.epimethix.lumicore.common.orm.sqlite.JoinOperator;
import com.github.epimethix.lumicore.orm.sql.SQLDialect;

public final class SQLSelectBuilder implements SelectBuilder {
	private final SQLDialect dialect;
	private final QueryBuilder previousBuilder;
	private String user;
	private Boolean closeConnection;
	private final String schemaName;
	private final Class<? extends Entity<?>> e;
	private final String tableName;
	private boolean distinct;
	private final StringBuilder selectionBuilder;
	private final StringBuilder joinBuilder;
	private final CriteriaBuilderImpl<SelectBuilder> criteriaBuilder;
	private final StringBuilder groupByBuilder;
	private final StringBuilder orderByBuilder;
	private String nulls;
	private Long limit;
	private Long defLimit = 100L;
	private Long offset;

	private int tableCounter;

	/*
	 * Key: the full table name (getTableName(String schemaName, Class<? extends
	 * Entity<?>> e)), Values: List of aliases
	 */
	private final Map<String, List<String>> aliasMap;
	private final String alias;

	private String putAlias(String schemaName, Class<? extends Entity<?>> e) {
		String entityName = SQLDialect.getTableName(schemaName, e);
		return putAlias(entityName);
	}

	private String putAlias(String entityName) {
		String alias = String.format("T%02d", ++tableCounter);
		List<String> aliases;
		if (!aliasMap.containsKey(entityName)) {
			aliases = new ArrayList<>();
			aliasMap.put(entityName, aliases);
		} else {
			aliases = aliasMap.get(entityName);
		}
		aliases.add(alias);
		return alias;
	}

	private String getAlias(String schemaName, Class<? extends Entity<?>> e) {
		String entityName = SQLDialect.getTableName(schemaName, e);
		if (aliasMap.containsKey(entityName)) {
			List<String> aliases = aliasMap.get(entityName);
			return aliases.get(0);
		}
		return putAlias(entityName);
	}

	private String getLastAlias(String schemaName, Class<? extends Entity<?>> e) {
		String entityName = SQLDialect.getTableName(schemaName, e);
		if (aliasMap.containsKey(entityName)) {
			List<String> aliases = aliasMap.get(entityName);
			return aliases.get(aliases.size() - 1);
		}
		return putAlias(entityName);
	}

	public SQLSelectBuilder(SQLSelectBuilder b) {
		this.dialect = b.dialect;
//		private final QueryBuilder previousBuilder;
		this.previousBuilder = b.previousBuilder;
//		private String user;
		this.user = b.user;
//		private Boolean closeConnection;
		this.closeConnection = b.closeConnection;
//		private final String schemaName;
		this.schemaName = b.schemaName;
//		private final Class<? extends Entity<?>> e;
		this.e = b.e;
//		private final String tableName;
		this.tableName = b.tableName;
//		private boolean distinct;
		this.distinct = b.distinct;
//		private final StringBuilder selectionBuilder;
		this.selectionBuilder = new StringBuilder(b.selectionBuilder);
//		private final StringBuilder joinBuilder;
		this.joinBuilder = new StringBuilder(b.joinBuilder);
//		private final CriteriaBuilder<SelectBuilder> criteriaBuilder;
		this.criteriaBuilder = new CriteriaBuilderImpl<>(b.criteriaBuilder, this);
//		private final StringBuilder groupByBuilder;
		this.groupByBuilder = new StringBuilder(b.groupByBuilder);
//		private final StringBuilder orderByBuilder;
		this.orderByBuilder = new StringBuilder(b.orderByBuilder);
//		private String nulls;
		this.nulls = b.nulls;
//		private Long limit;
		this.limit = b.limit;
//		private Long defLimit = 100L;
		this.defLimit = b.defLimit;
//		private Long offset;
		this.offset = b.offset;
		this.aliasMap = new HashMap<>(b.aliasMap);
		this.alias = b.alias;
		this.tableCounter = b.tableCounter;
//		private int tableCounter;
	}

	public SQLSelectBuilder(SQLDialect dialect, Class<? extends Entity<?>> e, String... selection) {
		this(dialect, null, "main", e, selection);
	}

	public SQLSelectBuilder(SQLDialect dialect, String schemaName, Class<? extends Entity<?>> e, String... selection) {
		this(dialect, null, schemaName, e, selection);
	}

	SQLSelectBuilder(SQLDialect dialect, QueryBuilder previousBuilder, String schemaName, Class<? extends Entity<?>> e,
			String... selection) {
		this.dialect = dialect;
		this.schemaName = schemaName;
		this.e = e;
		this.tableName = SQLDialect.getTableName(schemaName, e);
		this.aliasMap = new HashMap<>();
		this.alias = getAlias(schemaName, e);
		this.selectionBuilder = new StringBuilder();
//		boolean firstRound = true;
//		for (String field : selection) {
//			if (firstRound) {
//				firstRound = false;
//			} else {
//				selectionBuilder.append(", ");
//			}
//			selectionBuilder.append(field);
//		}
		select(alias, selection);
		this.previousBuilder = previousBuilder;
		this.joinBuilder = new StringBuilder();
		this.criteriaBuilder = new CriteriaBuilderImpl<>(this);
		this.groupByBuilder = new StringBuilder();
		this.orderByBuilder = new StringBuilder();
	}

	@Override
	public SelectBuilder withUser(String user) {
		this.user = user;
		return this;
	}

	@Override
	public SelectBuilder withCloseConnection(boolean closeConnection) {
		this.closeConnection = closeConnection;
		return this;
	}

	@Override
	public SelectBuilder distinct() {
		this.distinct = true;
		return this;
	}

	@Override
	public SelectBuilder select(String schemaName, Class<? extends Entity<?>> e, String... fields) {
		String alias = getLastAlias(schemaName, e);
		return select(alias, fields);
	}

	@Override
	public SelectBuilder select(String alias, String... fields) {
		for (String field : fields) {
			if (selectionBuilder.length() != 0) {
				selectionBuilder.append(", ");
			}
			selectionBuilder.append(alias).append(".`").append(field).append("`");
		}
		return this;
	}

	@Override
	public SelectBuilder selectCount(String schemaName, Class<? extends Entity<?>> e, String field) {
		String alias = getLastAlias(schemaName, e);
		if (selectionBuilder.length() != 0) {
			selectionBuilder.append(", ");
		}
		selectionBuilder.append("count(").append(alias).append(".`").append(field).append("`)");
		return this;
	}

	@Override
	public SelectBuilder selectSum(String schemaName, Class<? extends Entity<?>> e, String field) {
		String alias = getLastAlias(schemaName, e);
		if (selectionBuilder.length() != 0) {
			selectionBuilder.append(", ");
		}
		selectionBuilder.append("sum(").append(alias).append(".`").append(field).append("`)");
		return this;
	}

	@Override
	public SelectBuilder selectMin(String schemaName, Class<? extends Entity<?>> e, String field) {
		String alias = getLastAlias(schemaName, e);
		if (selectionBuilder.length() != 0) {
			selectionBuilder.append(", ");
		}
		selectionBuilder.append("min(").append(alias).append(".`").append(field).append("`)");
		return this;
	}

	@Override
	public SelectBuilder selectMax(String schemaName, Class<? extends Entity<?>> e, String field) {
		String alias = getLastAlias(schemaName, e);
		if (selectionBuilder.length() != 0) {
			selectionBuilder.append(", ");
		}
		selectionBuilder.append("max(").append(alias).append(".`").append(field).append("`)");
		return this;
	}

	@Override
	public SelectBuilder selectAverage(String schemaName, Class<? extends Entity<?>> e, String field) {
		String alias = getLastAlias(schemaName, e);
		if (selectionBuilder.length() != 0) {
			selectionBuilder.append(", ");
		}
		selectionBuilder.append("avg(").append(alias).append(".`").append(field).append("`)");
		return this;
	}

	@Override
	public CriteriaBuilder<SelectBuilder> withCriteria(String schemaName, Class<? extends Entity<?>> e) {
		String alias = getLastAlias(schemaName, e);
		return criteriaBuilder.withAlias(alias);
	}

	@Override
	public JoinBuilder join(String localSchemaName, Class<? extends Entity<?>> localTable, String otherSchemaName,
			Class<? extends Entity<?>> otherTable, String[] generatedAlias, JoinOperator... joinOperators) {
		return new SQLJoinBuilder(localSchemaName, localTable, otherSchemaName, otherTable, generatedAlias,
				joinOperators);
	}

	@Override
	public SelectBuilder groupBy(String schemaName, Class<? extends Entity<?>> e, String field, String... fields) {
		if (groupByBuilder.length() != 0) {
			groupByBuilder.append(", ");
		}
		String alias = getAlias(schemaName, e);
		groupByBuilder.append(alias).append(".`").append(field).append("`");
		for (String f : fields) {
			groupByBuilder.append(", ").append(alias).append(".`").append(f).append("`");
		}
		return this;
	}

	@Override
	public SelectBuilder orderByAsc(String schemaName, Class<? extends Entity<?>> e, String field) {
		return orderBy(field, Order.ASC);
	}

	@Override
	public SelectBuilder orderByDesc(String schemaName, Class<? extends Entity<?>> e, String field) {
		return orderBy(field, Order.DESC);
	}

	private SelectBuilder orderBy(String field, Order o) {
		if (orderByBuilder.length() != 0) {
			orderByBuilder.append(", ");
		}
		String alias = getAlias(schemaName, e);
		orderByBuilder.append(alias).append(".`").append(field).append("`");
		if (o == Order.DESC) {
			orderByBuilder.append(" DESC");
		}
		return this;

	}

	@Override
	public SelectBuilder nullsFirst() {
		nulls = "NULLS FIRST ";
		return this;
	}

	@Override
	public SelectBuilder nullsLast() {
		nulls = "NULLS LAST ";
		return this;
	}

	@Override
	public SelectBuilder clearLimit() {
		this.limit = null;
		this.defLimit = null;
		return this;
	}

	@Override
	public SelectBuilder limit(long limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public SelectBuilder offset(long offset) {
		this.offset = offset;
		return this;
	}

	@Override
	public SelectBuilder page(long page) {
		this.offset = limit * (page - 1);
		return this;
	}

	@Override
	public SelectQuery build() {
		Query prev = null;
		if (Objects.nonNull(previousBuilder)) {
			prev = previousBuilder.build();
		}
		if (Objects.nonNull(prev)) {
			/*
			 * Append INSERT or CREATE
			 */
			String user = prev.getUser().orElse(null);
			if (Objects.nonNull(user)) {
				withUser(user);
			}
			Boolean closeConnObj = prev.isCloseConnection().orElse(null);
			if (Objects.nonNull(closeConnObj)) {
				withCloseConnection(closeConnObj);
			}
		}
		String sql = dialect.compileSelect(prev, distinct, selectionBuilder, tableName, alias, joinBuilder,
				groupByBuilder, criteriaBuilder, orderByBuilder, nulls, limit, defLimit, offset);
		return new SQLSelectQuery(user, closeConnection, sql, criteriaBuilder.getCriteriumValues().toArray(), this);
	}

	private final class SQLJoinBuilder implements JoinBuilder {
		private final StringBuilder joinBuilder = new StringBuilder();
//		private final String localSchemaName;
		private final Class<? extends Entity<?>> localTable;
//		private final String otherSchemaName;
		private final Class<? extends Entity<?>> otherTable;
		private final String[] generatedAlias;
		private final String aliasLocal;
		private final String aliasOther;

		private SQLJoinBuilder(String localSchemaName, Class<? extends Entity<?>> localTable, String otherSchemaName,
				Class<? extends Entity<?>> otherTable, String[] generatedAlias, JoinOperator... joinOperators) {
//			this.localSchemaName = localSchemaName;
			this.localTable = localTable;
//			this.otherSchemaName = otherSchemaName;
			this.otherTable = otherTable;
			this.generatedAlias = generatedAlias;
			List<JoinOperator> j = new ArrayList<>(Arrays.asList(joinOperators));
			if (j.contains(JoinOperator.NATURAL)) {
				joinBuilder.append("NATURAL ");
			}
			if (j.contains(JoinOperator.LEFT)) {
				joinBuilder.append("LEFT ");
			} else if (j.contains(JoinOperator.RIGHT)) {
				joinBuilder.append("RIGHT ");
			} else if (j.contains(JoinOperator.FULL)) {
				joinBuilder.append("FULL ");
			}
			if (j.contains(JoinOperator.OUTER)) {
				joinBuilder.append("OUTER ");
			} else if (j.contains(JoinOperator.INNER)) {
				joinBuilder.append("INNER ");
			} else if (j.contains(JoinOperator.CROSS)) {
				joinBuilder.append("CROSS ");
			}
			if (j.contains(JoinOperator.JOIN)) {
				joinBuilder.append("JOIN ");
			}
			if (joinBuilder.length() == 0) {
				joinBuilder.append(", ");
			}
			if(localTable == otherTable) {
			this.aliasLocal = SQLSelectBuilder.this.getLastAlias(localSchemaName, localTable);
			} else {
				this.aliasLocal = SQLSelectBuilder.this.getAlias(localSchemaName, localTable);
			}
			this.aliasOther = SQLSelectBuilder.this.putAlias(otherSchemaName, otherTable);

			joinBuilder.append("`").append(otherSchemaName).append("`.`").append(Entity.getEntityName(otherTable))
					.append("` AS ").append(aliasOther).append(" ");
		}

		@Override
		public SelectBuilder on(String[] foreignKeys, String[] referencedFields) {
			if (Objects.nonNull(generatedAlias) && generatedAlias.length > 0) {
				generatedAlias[0] = aliasOther;
			}
			if (foreignKeys.length == 0 || foreignKeys.length != referencedFields.length) {
				throw new IllegalArgumentException(
						"foreignKeys and referencedFields may not be empty and must be of equal length!");
			}
			joinBuilder.append("ON ");
			for (int i = 0; i < foreignKeys.length; i++) {
				String fk = foreignKeys[i];
				String ref = referencedFields[i];
				if (i > 0) {
					joinBuilder.append("AND ");
				}
				joinBuilder.append(aliasLocal).append(".`").append(fk).append("` = ").append(aliasOther).append(".`")
						.append(ref).append("` ");
			}
			SQLSelectBuilder.this.joinBuilder.append(joinBuilder);
			return SQLSelectBuilder.this;
		}

		@Override
		public SelectBuilder using(String... columns) {
			throw new RuntimeException("NIY");
//			TODO return SQLiteSelectBuilder.this;
		}
	}
}