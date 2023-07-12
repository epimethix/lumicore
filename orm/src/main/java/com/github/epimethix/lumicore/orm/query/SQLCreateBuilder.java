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
import com.github.epimethix.lumicore.common.orm.query.Query.CreateBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.CreateQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectBuilder;
import com.github.epimethix.lumicore.common.orm.sqlite.Constraint;
import com.github.epimethix.lumicore.common.orm.sqlite.Definition;
import com.github.epimethix.lumicore.orm.sql.SQLDialect;

public final class SQLCreateBuilder implements CreateBuilder {
	private final SQLDialect dialect;
	private String user;
	private Boolean closeConnection;
	private final String schemaName;
	private final Class<? extends Entity<?>> e;
	private final String tableName;
	private final List<Definition> definitions;
	private final List<Constraint> constraints;
	private boolean withoutRowid;
	private boolean strict;
	private boolean temp;
	private boolean ifNotExists;

	public SQLCreateBuilder(SQLDialect dialect, Class<? extends Entity<?>> e, Definition... definitions) {
		this(dialect, "main", e, definitions);
	}

	public SQLCreateBuilder(SQLDialect dialect, String schemaName, Class<? extends Entity<?>> e, Definition... definitions) {
		this.dialect = dialect;
		this.schemaName = schemaName;
		this.e = e;
		this.tableName = SQLDialect.getTableName(schemaName, e);
		this.definitions = new ArrayList<>(Arrays.asList(definitions));
		this.constraints = new ArrayList<>();
	}

	public SQLCreateBuilder(SQLCreateBuilder sqLiteCreateBuilder) {
		this.dialect = sqLiteCreateBuilder.dialect;
		this.user = sqLiteCreateBuilder.user;
		this.closeConnection = sqLiteCreateBuilder.closeConnection;
		this.schemaName = sqLiteCreateBuilder.schemaName;
		this.e = sqLiteCreateBuilder.e;
		this.tableName = sqLiteCreateBuilder.tableName;
		this.definitions = new ArrayList<>(sqLiteCreateBuilder.definitions);
		this.constraints = new ArrayList<>(sqLiteCreateBuilder.constraints);
		this.withoutRowid = sqLiteCreateBuilder.withoutRowid;
		this.strict = sqLiteCreateBuilder.strict;
		this.temp = sqLiteCreateBuilder.temp;
		this.ifNotExists = sqLiteCreateBuilder.ifNotExists;
	}

	@Override
	public CreateBuilder withUser(String user) {
		this.user = user;
		return this;
	}

	@Override
	public CreateBuilder withCloseConnection(boolean closeConnection) {
		this.closeConnection = closeConnection;
		return this;
	}

	@Override
	public CreateBuilder temp() {
		this.temp = true;
		return this;
	}

	@Override
	public CreateBuilder ifNotExists() {
		this.ifNotExists = true;
		return this;
	}

	@Override
	public CreateBuilder withConstraints(Constraint... constraints) {
		this.constraints.addAll(Arrays.asList(constraints));
		return this;
	}

	@Override
	public CreateBuilder strict() {
		this.strict = true;
		return this;
	}

	@Override
	public CreateBuilder withoutRowid() {
		this.withoutRowid = true;
		return this;
	}

	@Override
	public SelectBuilder select(String schemaName, Class<? extends Entity<?>> e, String... fields) {
		return new SQLSelectBuilder(dialect, this, schemaName, e, fields);
	}

	@Override
	public SelectBuilder select(Class<? extends Entity<?>> e, String... fields) {
		return new SQLSelectBuilder(dialect, this, "main", e, fields);
	}

	@Override
	public CreateQuery build() {
		String sql = dialect.compileCreate(temp, ifNotExists, schemaName, tableName, definitions, constraints, strict,
				withoutRowid);
		return new SQLCreateQuery(user, closeConnection, sql, this);
	}
}