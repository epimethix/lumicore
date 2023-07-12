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

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.query.Query.CriteriaBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.DeleteBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.DeleteQuery;
import com.github.epimethix.lumicore.orm.sql.SQLDialect;

public final class SQLDeleteBuilder implements DeleteBuilder {
	private final SQLDialect dialect;
	private String user;
	private Boolean closeConnection;
	private final CriteriaBuilderImpl<DeleteBuilder> criteriaBuilder;
	private final String schemaName;
	private final Class<? extends Entity<?>> e;

	public SQLDeleteBuilder(SQLDialect dialect, String schemaName, Class<? extends Entity<?>> e) {
		this.dialect = dialect;
		this.schemaName = schemaName;
		this.e = e;
		criteriaBuilder = new CriteriaBuilderImpl<>(this);
	}

	public SQLDeleteBuilder(SQLDeleteBuilder b) {
		this.dialect = b.dialect;
		this.user = b.user;
		this.closeConnection = b.closeConnection;
		this.criteriaBuilder = new CriteriaBuilderImpl<>(b.criteriaBuilder, this);
		this.schemaName = b.schemaName;
		this.e = b.e;
	}

	@Override
	public DeleteBuilder withUser(String user) {
		this.user = user;
		return this;
	}

	@Override
	public DeleteBuilder withCloseConnection(boolean closeConnection) {
		this.closeConnection = closeConnection;
		return this;
	}

	@Override
	public CriteriaBuilder<DeleteBuilder> withCriteria(String schemaName, Class<? extends Entity<?>> e) {
		return criteriaBuilder.withAlias("T01");
	}

	@Override
	public DeleteQuery build() {
		String sql = dialect.compileDelete(schemaName, e, criteriaBuilder);
		return new SQLDeleteQuery(user, closeConnection, sql,
				criteriaBuilder.getCriteriumValues().toArray(), this);
	}
}