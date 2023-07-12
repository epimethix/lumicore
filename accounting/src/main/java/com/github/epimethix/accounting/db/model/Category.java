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

import com.github.epimethix.lumicore.common.orm.model.TreeEntity;
import com.github.epimethix.lumicore.orm.annotation.field.ManyToOne;
import com.github.epimethix.lumicore.orm.annotation.field.PrimaryKey;
import com.github.epimethix.lumicore.orm.annotation.field.Resolve;

public final class Category implements TreeEntity<Category, Long> {
	@PrimaryKey
	private final Long id;
	@Resolve(depth = 33)
	@ManyToOne
	private final Category parent;
	private final String name;
	private final String description;

	public Category(Long id, Category parent, String name, String description) {
		this.id = id;
		this.parent = parent;
		this.name = name;
		this.description = description;
	}

	public Category(Category category) {
		this.id = category.id;
		this.parent = category.parent;
		this.name = category.name;
		this.description = category.description;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public Category getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return name;
	}

	public static final class Builder implements EntityBuilder<Long> {
		private Long id;
		private Category parent;
		private String name;
		private String description;

		public Builder() {}

		public Builder(Category category) {
			this.id = category.id;
			this.parent = category.parent;
			this.name = category.name;
			this.description = category.description;
		}

		@Override
		public Builder setId(Long id) {
			this.id = id;
			return this;
		}

		public Builder setParent(Category parent) {
			this.parent = parent;
			return this;
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setDescription(String description) {
			this.description = description;
			return this;
		}

		@Override
		public Category build() {
			return new Category(id, parent, name, description);
		}
	}

	public Builder getBuilder() {
		return newBuilder(this);
	}

	public static final Builder newBuilder() {
		return new Builder();
	}

	public static final Builder newBuilder(Category category) {
		return new Builder(category);
	}
}
