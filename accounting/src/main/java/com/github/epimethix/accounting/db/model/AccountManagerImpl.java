/*
 *  Accounting - Lumicore example application
 *  Copyright (C) 2023  epimethix@protonmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.epimethix.accounting.db.model;

import com.github.epimethix.accounting.db.model.lazy.AccountManager;
import com.github.epimethix.lumicore.orm.annotation.field.PrimaryKey;

public final class AccountManagerImpl implements AccountManager {
	@PrimaryKey
	private final Long id;
	private final String name;
	private final String email;
	private final String phone;

	public AccountManagerImpl(Long id, String name, String email, String phone) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.phone = phone;
	}

	public AccountManagerImpl(AccountManagerImpl accountManager) {
		this.id = accountManager.id;
		this.name = accountManager.name;
		this.email = accountManager.email;
		this.phone = accountManager.phone;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public String getPhone() {
		return phone;
	}

	@Override
	public String toString() {
		return name;
	}

	public static final class Builder implements EntityBuilder<Long> {

		private Long id;
		private String name;
		private String email;
		private String phone;

		public Builder() {}

		public Builder(AccountManagerImpl accountManager) {
			this.id = accountManager.id;
			this.name = accountManager.name;
			this.email = accountManager.email;
			this.phone = accountManager.phone;
		}

		@Override
		public Builder setId(Long id) {
			this.id = id;
			return this;
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setEmail(String email) {
			this.email = email;
			return this;
		}

		public Builder setPhone(String phone) {
			this.phone = phone;
			return this;
		}

		@Override
		public AccountManagerImpl build() {
			return new AccountManagerImpl(id, name, email, phone);
		}
	}

	public Builder getBuilder() {
		return newBuilder(this);
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static Builder newBuilder(AccountManagerImpl accountManager) {
		return new Builder(accountManager);
	}
}
