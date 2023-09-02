package com.github.epimethix.lumicore.orm.example.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.orm.annotation.field.OneToMany;
import com.github.epimethix.lumicore.orm.annotation.field.PrimaryKey;

public final class MyImmutableEntity implements Entity<Long> {
	@PrimaryKey
	private final Long id;
	private final String name;
	private final String email;
	@OneToMany
	private final List<MyMutableEntity> mutableEntities;

	public MyImmutableEntity(Long id, String name, String email, List<MyMutableEntity> mutableEntities) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.mutableEntities = Collections.unmodifiableList(mutableEntities);
	}

	public MyImmutableEntity(MyImmutableEntity myEntity) {
		/*
		 * All Immutable
		 */
		this.id = myEntity.id;
		this.name = myEntity.name;
		this.email = myEntity.email;
		this.mutableEntities = Collections.unmodifiableList(myEntity.mutableEntities);
	}

	@Override
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}
	
	public List<MyMutableEntity> getMutableEntities() {
		return mutableEntities;
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, id, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyImmutableEntity other = (MyImmutableEntity) obj;
		return Objects.equals(email, other.email) && Objects.equals(id, other.id) && Objects.equals(name, other.name);
	}

	public final class Builder implements EntityBuilder<Long> {
		private Long id;
		private String name;
		private String email;
		private List<MyMutableEntity> mutableEntities;

		public Builder() {}

		public Builder(MyImmutableEntity myEntity) {
			/*
			 * All Immutable
			 */
			this.id = myEntity.id;
			this.name = myEntity.name;
			this.email = myEntity.email;
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

		public Builder setMutableEntities(List<MyMutableEntity> mutableEntities) {
			this.mutableEntities = mutableEntities;
			return this;
		}

		@Override
		public Entity<Long> build() {
			return new MyImmutableEntity(id, name, email, mutableEntities);
		}
	}
}
