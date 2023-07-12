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
package com.github.epimethix.lumicore.common.orm.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.properties.LumicoreProperties;
@Deprecated
public class QueryParameters {
	private final boolean closeConnection;
//	private final boolean mutable;
	private final String userName;
	private final long limit;
	private final long offset;
	private final List<String> whereFields;
	private final List<Object> whereValues;
	private final List<String> orderBy;
	private final List<Boolean> orderByAscending;

	private QueryParameters(boolean closeConnection, String userName, long limit, long offset,
			List<String> whereFields, List<Object> whereValues, List<String> orderBy, List<Boolean> orderByAscending) {
		super();
		this.closeConnection = closeConnection;
//		this.mutable = mutable;
		this.userName = userName;
		this.limit = limit;
		this.offset = offset;
		this.whereFields = Collections.unmodifiableList(whereFields);
		this.whereValues = Collections.unmodifiableList(whereValues);
		this.orderBy = Collections.unmodifiableList(orderBy);
		this.orderByAscending = Collections.unmodifiableList(orderByAscending);
	}

	public boolean isCloseConnection() {
		return closeConnection;
	}

//	public boolean isMutable() {
//		return mutable;
//	}

	public String getUserName() {
		return userName;
	}

	public long getLimit() {
		return limit;
	}

	public long getOffset() {
		return offset;
	}

	public List<String> getWhereFields() {
		return whereFields;
	}

	public List<Object> getWhereValues() {
		return whereValues;
	}

	public List<String> getOrderBy() {
		return orderBy;
	}

	public List<Boolean> getOrderByAscending() {
		return orderByAscending;
	}
	public static final class Builder {

		private boolean closeConnection = LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION;
		private String userName;
		private long limit = LumicoreProperties.DEFAULT_QUERY_LIMIT;
		private long offset = 0L;
		private List<String> whereFields = new ArrayList<>();
		private List<Object> whereValues = new ArrayList<>();
		private List<String> orderBy = new ArrayList<>();
		private List<Boolean> orderByAscending = new ArrayList<>();

		private Builder(Database database) {
			this.userName = database.getActiveUser();
		}

		private Builder(QueryParameters params) {
			this.closeConnection = params.closeConnection;
//			this.mutable = params.mutable;
			this.userName = params.userName;
			this.limit = params.limit;
			this.offset = params.offset;
			this.whereFields = new ArrayList<>(params.whereFields);
			this.whereValues = new ArrayList<>(params.whereValues);
		}

		public Builder closeConnection(boolean closeConnection) {
			this.closeConnection = closeConnection;
			return this;
		}

//		public Builder mutable(boolean mutable) {
//			this.mutable = mutable;
//			return this;
//		}

		public Builder userName(String userName) {
			this.userName = userName;
			return this;
		}

		public Builder limit(long limit) {
			this.limit = limit;
			return this;
		}

		public Builder offset(long offset) {
			this.offset = offset;
			return this;
		}

		public Builder whereFields(String... fields) {
			if (fields.length > 0) {
				for (String field : fields) {
					whereFields.add(field);
				}
			}
			return this;
		}

		public Builder whereValues(Object... values) {
			if (values.length > 0) {
				for (Object value : values) {
					whereValues.add(value);
				}
			}
			return this;
		}

		public Builder orderBy(String field) {
			return orderBy(field, true);
		}

		public Builder orderBy(String field, boolean ascending) {
			orderBy.add(field);
			orderByAscending.add(ascending);
			return this;
		}

		public QueryParameters build() {
			return new QueryParameters(closeConnection, userName, limit, offset, whereFields, whereValues,
					orderBy, orderByAscending);
		}

	}

	public static final Builder newBuilder(QueryParameters params) {
		return new Builder(params);
	}

	public static final Builder newBuilder(Database database) {
		return new Builder(database);
	}

	public static QueryParameters defaults(Database database) {
		return newBuilder(database).build();
	}
}
