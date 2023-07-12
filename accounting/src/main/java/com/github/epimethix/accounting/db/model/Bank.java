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

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.orm.annotation.field.PrimaryKey;

public final class Bank implements Entity<Long> {
	@PrimaryKey
	private final Long id;
	private final String name;
	private final String bankCode;

	public Bank(Long id, String name, String bankCode) {
		this.id = id;
		this.name = name;
		this.bankCode = bankCode;
	}

	public Bank(Bank bank) {
		this.id = bank.id;
		this.name = bank.name;
		this.bankCode = bank.bankCode;
	}

	@Override
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getBankCode() {
		return bankCode;
	}

	@Override
	public String toString() {
		return String.format("%s - %s", name, bankCode);
	}

	public Builder getBuilder() {
		return newBuilder(this);
	}

	public static final class Builder implements EntityBuilder<Long> {

		private Long id;
		private String name;
		private String bankCode;

		public Builder() {}

		public Builder(Bank bank) {
			this.id = bank.id;
			this.name = bank.name;
			this.bankCode = bank.bankCode;
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

		public Builder setBankCode(String bankCode) {
			this.bankCode = bankCode;
			return this;
		}

		@Override
		public Bank build() {
			return new Bank(id, name, bankCode);
		}
	}

	public static final Builder newBuilder() {
		return new Builder();
	}

	public static final Builder newBuilder(Bank bank) {
		return new Builder(bank);
	}
}
