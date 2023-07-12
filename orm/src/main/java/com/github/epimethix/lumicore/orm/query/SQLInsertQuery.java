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

import java.util.List;

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.query.Query.InsertQuery;

final class SQLInsertQuery extends DefaultQuery implements InsertQuery {
	private String[] fields;
	private List<? extends Entity<?>> records;
	private final SQLInsertBuilder b;

	 SQLInsertQuery(String user, Boolean isCloseConnection, String queryString,
			List<? extends Entity<?>> records, String[] fields, SQLInsertBuilder b) {
		super(user, isCloseConnection, queryString);
		this.records = records;
		this.fields = fields;
		this.b = b;
	}

	@Override
	public String[] getFields() {
		return fields;
	}

	@Override
	public List<? extends Entity<?>> getRecords() {
		return records;
	}

	@Override
	public InsertQuery withRecords(List<? extends Entity<?>> records) {
		return new SQLInsertQuery(getUser().orElse(null), isCloseConnection().orElse(null), getQueryString(),
				records, fields, b);
	}

	@Override
	public InsertBuilder builder() {
		return new SQLInsertBuilder(b);
	}
}