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

import com.github.epimethix.lumicore.common.orm.query.Query.UpdateQuery;

final class SQLUpdateQuery extends DefaultQuery<UpdateQuery> implements UpdateQuery {

	private final String[] setFields;
	private final Object[] setValues;
	private final Object[] criteriumValues;
	private final SQLUpdateBuilder b;

	public SQLUpdateQuery(String user, Boolean isCloseConnection, String queryString, String[] setFields,
			Object[] setValues, Object[] criteriumValues, SQLUpdateBuilder b) {
		super(user, isCloseConnection, queryString);
		this.setFields = setFields;
		this.setValues = setValues;
		this.criteriumValues = criteriumValues;
		this.b = b;

	}

	@Override
	public String[] getFields() {
		return setFields;
	}

	@Override
	public Object[] getSetValues() {
		return setValues;
	}

	@Override
	public Object[] getCriteriumValues() {
		return criteriumValues;
	}

	@Override
	public String[] getCriteriumFields() {
		return b.getCriteriumFields();
	}

	@Override
	public UpdateBuilder builder() {
		return new SQLUpdateBuilder(b);
	}

	@Override
	public UpdateQuery withValues(Object... values) {
		return new SQLUpdateQuery(getUser().orElse(null), isCloseConnection().orElse(null), getQueryString(), setFields,
				values, criteriumValues, b);
	}

	@Override
	public UpdateQuery withCriteriumValues(Object... values) {
		return new SQLUpdateQuery(getUser().orElse(null), isCloseConnection().orElse(null), getQueryString(), setFields,
				setValues, values, b);
	}
}
