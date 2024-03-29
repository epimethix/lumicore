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

import java.util.Objects;
import java.util.Optional;

import com.github.epimethix.lumicore.common.orm.query.Query;

abstract class DefaultQuery<Q extends Query<Q>> implements Query<Q> {

	private final String user;
	private final Boolean isCloseConnection;
	private final String queryString;

	public DefaultQuery(String user, Boolean isCloseConnection, String queryString) {
		this.user = user;
		this.isCloseConnection = isCloseConnection;
		this.queryString = Objects.requireNonNull(queryString);
	}

	@Override
	public Optional<String> getUser() {
		return Optional.ofNullable(user);
	}

	@Override
	public Optional<Boolean> isCloseConnection() {
		return Optional.ofNullable(isCloseConnection);
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

}
