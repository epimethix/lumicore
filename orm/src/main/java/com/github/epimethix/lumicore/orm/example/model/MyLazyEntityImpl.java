package com.github.epimethix.lumicore.orm.example.model;

import java.time.LocalDate;

public class MyLazyEntityImpl implements MyLazyEntity {

	private final Long id;

	private final Long count;

	private final LocalDate date;

	private final String comment;

	public MyLazyEntityImpl(MyLazyEntity myLazyEntity) {
		this.id = myLazyEntity.getId();
		this.count = myLazyEntity.getCount();
		this.date = myLazyEntity.getDate();
		this.comment = myLazyEntity.getComment();
	}

	public MyLazyEntityImpl(Long id, Long count, LocalDate date, String comment) {
		this.id = id;
		this.count = count;
		this.date = date;
		this.comment = comment;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public Long getCount() {
		return count;
	}

	@Override
	public LocalDate getDate() {
		return date;
	}

	@Override
	public String getComment() {
		return comment;
	}

	public static final class Builder implements EntityBuilder<Long> {

		private Long id;

		private Long count;

		private LocalDate date;

		private String comment;

		public Builder() {}

		public Builder(MyLazyEntity myLazyEntity) {
			this.id = myLazyEntity.getId();
			this.count = myLazyEntity.getCount();
			this.date = myLazyEntity.getDate();
			this.comment = myLazyEntity.getComment();
		}

		@Override
		public Builder setId(Long id) {
			this.id = id;
			return this;
		}

		public Builder setCount(Long count) {
			this.count = count;
			return this;
		}

		public Builder setDate(LocalDate date) {
			this.date = date;
			return this;
		}

		public Builder setComment(String comment) {
			this.comment = comment;
			return this;
		}
		
		@Override
		public MyLazyEntity build() {
			return new MyLazyEntityImpl(id, count, date, comment);
		}
	}
}
