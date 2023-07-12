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

import java.math.BigDecimal;
import java.util.Objects;

import com.github.epimethix.accounting.db.model.lazy.AccountManager;
import com.github.epimethix.lumicore.orm.annotation.field.ManyToOne;
import com.github.epimethix.lumicore.orm.model.AbstractIntegerEntity;

public final class Account extends AbstractIntegerEntity {
	private final String name;
	private final String BIC;
	private final String IBAN;
	@ManyToOne(lazy = true)
	private final AccountManager accountManager;
	@ManyToOne
	private final Bank bank;
	private BigDecimal balance;

	public Account(Long id, String name, String bIC, String iBAN, AccountManager accountManager, Bank bank,
			BigDecimal balance) {
		super(id);
		this.name = name;
		this.BIC = bIC;
		this.IBAN = iBAN;
		this.accountManager = accountManager;
		this.bank = bank;
		this.balance = balance;
	}

	public Account(Account account) {
		super(account.getId());
		this.name = account.name;
		this.BIC = account.BIC;
		this.IBAN = account.IBAN;
		this.accountManager = account.accountManager;
		this.bank = account.bank;
		this.balance = account.balance;
	}

	public String getName() {
		return name;
	}

	public String getBIC() {
		return BIC;
	}

	public String getIBAN() {
		return IBAN;
	}

	public AccountManager getAccountManager() {
		return accountManager;
	}

	public Bank getBank() {
		return bank;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	@Override
	public String toString() {
		return name;
	}

	public static final class Builder implements EntityBuilder<Long> {
		private Long id;
		private String name;
		private String BIC;
		private String IBAN;
		private AccountManager accountManager;
		private Bank bank;
		private BigDecimal balance;

		public Builder() {}

		public Builder(Account account) {
			if (Objects.nonNull(account)) {
				this.id = account.getId();
				this.name = account.name;
				this.BIC = account.BIC;
				this.IBAN = account.IBAN;
				this.accountManager = account.accountManager;
				this.bank = account.bank;
				this.balance = account.balance;
			}
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

		public Builder setBIC(String BIC) {
			this.BIC = BIC;
			return this;
		}

		public Builder setIBAN(String IBAN) {
			this.IBAN = IBAN;
			return this;
		}

		public Builder setAccountManager(AccountManager accountManager) {
			this.accountManager = accountManager;
			return this;
		}

		public Builder setBank(Bank bank) {
			this.bank = bank;
			return this;
		}

		public Builder setBalance(BigDecimal balance) {
			this.balance = balance;
			return this;
		}

		@Override
		public Account build() {
			return new Account(id, name, BIC, IBAN, accountManager, bank, balance);
		}
	}

	public Builder getBuilder() {
		return newBuilder(this);
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static Builder newBuilder(Account account) {
		return new Builder(account);
	}
}
