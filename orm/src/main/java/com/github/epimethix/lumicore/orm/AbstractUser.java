/*
 * Copyright 2021-2022 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.orm;

import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.sqlite.Constraint;
import com.github.epimethix.lumicore.common.orm.sqlite.Definition;
import com.github.epimethix.lumicore.common.orm.sqlite.Constraint.UniqueConstraint;
import com.github.epimethix.lumicore.orm.annotation.field.Column;
import com.github.epimethix.lumicore.orm.model.i.UserEntity;
import com.github.epimethix.lumicore.stackutil.AccessCheck;

public abstract class AbstractUser<T> implements UserEntity<T> {

	// @formatter:off
	private static final AccessCheck AC_ABSTRACT_REPOSITORY = AccessCheck.Builder.newBuilder()
			.allowIntermediateCaller("jdk.internal.reflect.NativeMethodAccessorImpl::invoke0")
			.allowIntermediateCaller("jdk.internal.reflect.NativeMethodAccessorImpl::invoke")
			.allowIntermediateCaller("jdk.internal.reflect.DelegatingMethodAccessorImpl::invoke")
			.allowIntermediateCaller("java.lang.reflect.Method::invoke")
			.allowCaller("com.github.epimethix.lumicore.orm.AbstractRepository::")
			.build();
	// @formatter:on
	
	public static final String USERNAME = "username";
	@Column(name = USERNAME, nullHandling = Definition.NOT_NULL)
	private String username;

	public static final String SECRET = "secret";
	@Column(name = SECRET, type = Definition.TYPE_BLOB)
	private String secret;

	public static final String SECRET_DATE = "secretDate";
	@Column(name = SECRET_DATE)
	private long secretDate;
	
	public static final String EXPIRY_DATE = "expiryDate";
	@Column(name = EXPIRY_DATE)
	private Long expiryDate;

	public static final UniqueConstraint UNIQUE_USERNAME = Constraint.uniqueConstraint(USERNAME);

	public AbstractUser() {}

	@Override
	public String getUserName() {
		return username;
	}

//	@Override
	void setUserName(String username) {
		this.username = username;
	}

	String getSecret() throws IllegalAccessException {
		AC_ABSTRACT_REPOSITORY.checkPermission();
		return secret;
	}

	void setSecret(String string) throws IllegalAccessException {
		AC_ABSTRACT_REPOSITORY.checkPermission();
		this.secret = string;
	}

	long getSecretDate() {
		return secretDate;
	}

	void setSecretDate(long secretDate) {
		this.secretDate = secretDate;
	}

	Long getExpiryDate() {
		return expiryDate;
	}

	void setExpiryDate(Long expiryDate) {
		this.expiryDate = expiryDate;
	}

	public boolean isExpired() {
		if (Objects.nonNull(getExpiryDate()) && getExpiryDate().longValue() < System.currentTimeMillis()) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expiryDate == null) ? 0 : expiryDate.hashCode());
		result = prime * result + ((secret == null) ? 0 : secret.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractUser<?> other = (AbstractUser<?>) obj;
		if (expiryDate == null) {
			if (other.expiryDate != null)
				return false;
		} else if (!expiryDate.equals(other.expiryDate))
			return false;
		if (secret == null) {
			if (other.secret != null)
				return false;
		} else if (!secret.equals(other.secret))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
}
