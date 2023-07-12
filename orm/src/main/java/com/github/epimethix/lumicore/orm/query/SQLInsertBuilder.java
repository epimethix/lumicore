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
import java.util.List;

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.query.Query.InsertBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.InsertQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectBuilder;
import com.github.epimethix.lumicore.orm.sql.SQLDialect;

public final class SQLInsertBuilder implements InsertBuilder {
	private final SQLDialect dialect;
	private String user;
	private Boolean closeConnection;
	private final String schemaName;
	private final Class<? extends Entity<?>> e;
	private final List<Entity<?>> records;
	private final String[] fields;

	public SQLInsertBuilder(SQLDialect dialect, String schemaName, Class<? extends Entity<?>> e, String... fields) {
		this.dialect = dialect;
		records = new ArrayList<>();
		this.schemaName = schemaName;
		this.e = e;
		this.fields = fields;
	}

	public SQLInsertBuilder(SQLInsertBuilder b) {
		this.dialect = b.dialect;
		this.user = b.user;
		this.closeConnection = b.closeConnection;
		this.schemaName = b.schemaName;
		this.e = b.e;
		this.records = new ArrayList<>(b.records);
		this.fields = Arrays.copyOf(b.fields, b.fields.length);
	}

	@Override
	public InsertBuilder withUser(String user) {
		this.user = user;
		return this;
	}

	@Override
	public InsertBuilder withCloseConnection(boolean closeConnection) {
		this.closeConnection = closeConnection;
		return this;
	}

	@Override
	public InsertBuilder values(List<? extends Entity<?>> records) {
		this.records.addAll(records);
		return this;
	}

	@Override
	public SelectBuilder select(String schemaName, Class<? extends Entity<?>> entity, String... fields) {
		return new SQLSelectBuilder(dialect, this, schemaName, entity, fields);
	}

	@Override
	public InsertQuery build() {
		String sql = dialect.compileInsert(schemaName, e, fields, records);
		return new SQLInsertQuery(user, closeConnection, sql, records, fields, this);
	}

}
