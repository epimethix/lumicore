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
import java.time.LocalDateTime;
import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.orm.annotation.field.ManyToOne;
import com.github.epimethix.lumicore.orm.annotation.field.PrimaryKey;

public final class Transaction implements Entity<Long> {
	public static enum Direction {
		EXPENSE, REVENUE
	}

	@PrimaryKey
	private final Long id;
	private final Direction direction;
	private final LocalDateTime time;
	private final BigDecimal amount;
	private final String bookingText;
	@ManyToOne
	private final Category category;
	@ManyToOne
	private final Account account;

	public Transaction(Long id, Direction direction, LocalDateTime time, BigDecimal amount, String bookingText,
			Category category, Account account) {
		this.id = id;
		this.direction = direction;
		this.time = time;
		this.amount = amount;
		this.bookingText = bookingText;
		this.category = category;
		this.account = account;
	}

	public Transaction(Transaction transaction) {
		this.id = transaction.id;
		this.direction = transaction.direction;
		this.time = transaction.time;
		this.amount = transaction.amount;
		this.bookingText = transaction.bookingText;
		this.category = transaction.category;
		this.account = transaction.account;
	}

	@Override
	public Long getId() {
		return id;
	}

	public Direction getDirection() {
		return direction;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getBookingText() {
		return bookingText;
	}

	public Category getCategory() {
		return category;
	}

	public Account getAccount() {
		return account;
	}

	@Override
	public String toString() {
		if (Objects.nonNull(amount))
			return bookingText + " " + (direction == Direction.REVENUE ? "+" : "-") + amount.toString();
		else
			return bookingText + " " + (direction == Direction.REVENUE ? "+" : "-") + "---";
	}

	public static final class Builder implements EntityBuilder<Long> {
		private Long id;
		private Direction direction;
		private LocalDateTime time;
		private BigDecimal amount;
		private String bookingText;
		private Category category;
		private Account account;

		public Builder() {}

		public Builder(Transaction transaction) {
			this.id = transaction.id;
			this.direction = transaction.direction;
			this.time = transaction.time;
			this.amount = transaction.amount;
			this.bookingText = transaction.bookingText;
			this.category = transaction.category;
			this.account = transaction.account;
		}

		@Override
		public Builder setId(Long id) {
			this.id = id;
			return this;
		}

		public Builder setDirection(Direction direction) {
			this.direction = direction;
			return this;
		}

		public Builder setTime(LocalDateTime time) {
			this.time = time;
			return this;
		}

		public Builder setAmount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public Builder setBookingText(String bookingText) {
			this.bookingText = bookingText;
			return this;
		}

		public Builder setCategory(Category category) {
			this.category = category;
			return this;
		}

		public Builder setAccount(Account account) {
			this.account = account;
			return this;
		}

		@Override
		public Entity<Long> build() {
			return new Transaction(id, direction, time, amount, bookingText, category, account);
		}
	}

	public Builder getBuilder() {
		return newBuilder(this);
	}

	public static final Builder newBuilder() {
		return new Builder();
	}

	public static final Builder newBuilder(Transaction transaction) {
		return new Builder(transaction);
	}
}
