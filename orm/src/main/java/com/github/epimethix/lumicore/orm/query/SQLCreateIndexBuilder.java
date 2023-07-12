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

import java.util.Arrays;

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.query.Query.CreateIndexBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.CreateIndexQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.CriteriaBuilder;
import com.github.epimethix.lumicore.orm.sql.SQLDialect;

public final class SQLCreateIndexBuilder implements CreateIndexBuilder {
	private final SQLDialect dialect;
	private String user;
	private Boolean closeConnection;
	private final String schemaName;
	private final Class<? extends Entity<?>> e;
	private final String indexName;
	private final CriteriaBuilderImpl<CreateIndexBuilder> criteriaBuilder;
	private boolean unique;
	private boolean ifNotExists;
	private String[] fields;

	public SQLCreateIndexBuilder(SQLDialect dialect, String schemaName, Class<? extends Entity<?>> e, String indexName,
			String... fields) {
		this.dialect = dialect;
		this.schemaName = schemaName;
		this.e = e;
		this.indexName = indexName;
		this.criteriaBuilder = new CriteriaBuilderImpl<>(this);
		this.fields = fields;

	}

	public SQLCreateIndexBuilder(SQLCreateIndexBuilder b) {
		this.dialect = b.dialect;
		this.user = b.user;
		this.closeConnection = b.closeConnection;
		this.schemaName = b.schemaName;
		this.e = b.e;
		this.indexName = b.indexName;
		this.criteriaBuilder = new CriteriaBuilderImpl<>(b.criteriaBuilder, this);
		this.unique = b.unique;
		this.ifNotExists = b.ifNotExists;
		this.fields = Arrays.copyOf(b.fields, b.fields.length);
	}

	@Override
	public CreateIndexBuilder withUser(String user) {
		this.user = user;
		return this;
	}

	@Override
	public CreateIndexBuilder withCloseConnection(boolean closeConnection) {
		this.closeConnection = closeConnection;
		return this;
	}

	@Override
	public CreateIndexBuilder unique() {
		this.unique = true;
		return this;
	}

	@Override
	public CreateIndexBuilder ifNotExists() {
		this.ifNotExists = true;
		return this;
	}

	@Override
	public CriteriaBuilder<CreateIndexBuilder> withCriteria(String schemaName, Class<? extends Entity<?>> e) {
		return criteriaBuilder.withAlias("T01");
	}

	@Override
	public CreateIndexQuery build() {
		String sql = dialect.compileCreateIndex(unique, ifNotExists, indexName, schemaName, e, fields, criteriaBuilder);
		return new SQLCreateIndexQuery(user, closeConnection, sql,
				criteriaBuilder.getCriteriumValues().toArray(), this);
	}
}
