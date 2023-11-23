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
import java.util.List;

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.query.Query.CriteriaBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.UpdateBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.UpdateQuery;
import com.github.epimethix.lumicore.orm.sql.SQLDialect;

public final class SQLUpdateBuilder implements UpdateBuilder {
	private final SQLDialect dialect;
	private String user;
	private Boolean closeConnection;
	private final CriteriaBuilderImpl<UpdateBuilder, UpdateQuery> criteriaBuilder;
	private final List<String> fields;
	private final List<Object> setValues;
	private final String schemaName;
	private final Class<? extends Entity<?>> e;

	public SQLUpdateBuilder(SQLDialect dialect, String schemaName, Class<? extends Entity<?>> e) {
		this.dialect = dialect;
		this.schemaName = schemaName;
		this.e = e;
		this.criteriaBuilder = new CriteriaBuilderImpl<>(this);
		this.fields = new ArrayList<>();
		this.setValues = new ArrayList<>();
	}

	public SQLUpdateBuilder(SQLUpdateBuilder b) {
		this.dialect = b.dialect;
		this.user = b.user;
		this.closeConnection = b.closeConnection;
		this.criteriaBuilder = new CriteriaBuilderImpl<>(b.criteriaBuilder, this);
		this.fields = new ArrayList<>(b.fields);
		this.setValues = new ArrayList<>(b.setValues);
		this.schemaName = b.schemaName;
		this.e = b.e;

	}

	@Override
	public UpdateBuilder withUser(String user) {
		this.user = user;
		return this;
	}

	@Override
	public UpdateBuilder withCloseConnection(boolean closeConnection) {
		this.closeConnection = closeConnection;
		return this;
	}

	@Override
	public UpdateBuilder set(String field, Object value) {
		fields.add(field);
		setValues.add(value);
		return this;
	}

	@Override
	public CriteriaBuilder<UpdateBuilder, UpdateQuery> withCriteria(String schemaName, Class<? extends Entity<?>> e) {
		return criteriaBuilder.withAlias("T01");
	}

	@Override
	public UpdateQuery build() {
		String sql = dialect.compileUpdate(schemaName, e, fields.toArray(new String[] {}), criteriaBuilder);
		return new SQLUpdateQuery(user, closeConnection, sql, fields.toArray(new String[] {}), setValues.toArray(),
				criteriaBuilder.getCriteriumValues().toArray(), this);
	}

	public String[] getCriteriumFields() {
		return criteriaBuilder.getCriteriumFields();
	}
}