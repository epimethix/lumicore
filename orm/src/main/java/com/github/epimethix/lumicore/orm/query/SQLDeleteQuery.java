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

import com.github.epimethix.lumicore.common.orm.query.Query.DeleteQuery;

final class SQLDeleteQuery extends DefaultQuery<DeleteQuery> implements DeleteQuery {
	private final Object[] criteriumValues;
	private final SQLDeleteBuilder b;

	 SQLDeleteQuery(String user, Boolean isCloseConnection, String queryString,
			Object[] criteriumValues, SQLDeleteBuilder b) {
		super(user, isCloseConnection, queryString);
		this.criteriumValues = criteriumValues;
		this.b = b;
	}

	@Override
	public Object[] getCriteriumValues() {
		return criteriumValues;
	}

	@Override
	public DeleteQuery withCriteriumValues(Object... values) {
		return new SQLDeleteQuery(getUser().orElse(null), isCloseConnection().orElse(null), getQueryString(),
				values, b);
	}

	@Override
	public DeleteBuilder builder() {
		return new SQLDeleteBuilder(b);
	}
}