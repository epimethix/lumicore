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

import com.github.epimethix.lumicore.orm.annotation.field.Column;
import com.github.epimethix.lumicore.orm.model.i.MetaEntity;
import com.github.epimethix.lumicore.stackutil.AccessCheck;

/**
 * Extending {@link AbstractMetaEntity} this entity class enables record meta
 * data tracking by Repository.
 * 
 * @param <ID> the unique identifier type of the {@code Entity<T>}
 *             implementation
 * 
 * @see MetaEntity
 */
public abstract class AbstractMetaEntity<ID> implements MetaEntity<ID> {
	// @formatter:off
	private static final AccessCheck AC_ABSTRACT_REPOSITORY = AccessCheck.Builder.createAllowAbstractRepositoryViaReflection();
//	private static final AccessCheck AC_ABSTRACT_REPOSITORY = AccessCheck.Builder.newBuilder()
//			.allowCaller("com.github.epimethix.lumicore.orm.AbstractRepository::")
//			.build();
	// @formatter:on

	@Column(name = CREATE_DATE)
	private final long createDate;

	@Column(name = READ_DATE)
	private final long readDate;

	@Column(name = UPDATE_DATE)
	private final long updateDate;

	@Column(name = DELETE_DATE)
	private final long deleteDate;

	private AbstractMetaEntity(long createDate, long readDate, long updateDate, long deleteDate) {
		this.createDate = createDate;
		this.readDate = readDate;
		this.updateDate = updateDate;
		this.deleteDate = deleteDate;
	}

	protected AbstractMetaEntity(AbstractMetaEntity<ID> metaEntity) {
		this.createDate = metaEntity.createDate;
		this.readDate = metaEntity.readDate;
		this.updateDate = metaEntity.updateDate;
		this.deleteDate = metaEntity.deleteDate;
	}

	@Override
	public final long getCreateDate() {
		return createDate;
	}

	@Override
	public final long getReadDate() {
		return readDate;
	}

	@Override
	public final long getUpdateDate() {
		return updateDate;
	}

	@Override
	public final long getDeleteDate() {
		return deleteDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (createDate ^ (createDate >>> 32));
		result = prime * result + (int) (deleteDate ^ (deleteDate >>> 32));
		result = prime * result + (int) (readDate ^ (readDate >>> 32));
		result = prime * result + (int) (updateDate ^ (updateDate >>> 32));
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
		AbstractMetaEntity other = (AbstractMetaEntity) obj;
		if (createDate != other.createDate)
			return false;
		if (deleteDate != other.deleteDate)
			return false;
		if (readDate != other.readDate)
			return false;
		if (updateDate != other.updateDate)
			return false;
		return true;
	}

	public static abstract class MetaBuilder<ID> implements EntityBuilder<ID> {
		private long createDate;
		private long readDate;
		private long updateDate;
		private long deleteDate;
		protected MetaBuilder(AbstractMetaEntity<ID> builder) {
			createDate = builder.createDate;
			readDate = builder.readDate;
			updateDate = builder.updateDate;
			deleteDate = builder.deleteDate;
		}

		final void setCreateDate(long date) throws IllegalAccessException {
			AC_ABSTRACT_REPOSITORY.checkPermission();
			this.createDate = date;
		}

		final void setReadDate(long date) throws IllegalAccessException {
			AC_ABSTRACT_REPOSITORY.checkPermission();
			this.readDate = date;
		}

		final void setUpdateDate(long date) throws IllegalAccessException {
			AC_ABSTRACT_REPOSITORY.checkPermission();
			this.updateDate = date;
		}

		final void setDeleteDate(long deleteDate) throws IllegalAccessException {
			AC_ABSTRACT_REPOSITORY.checkPermission();
			this.deleteDate = deleteDate;
		}
	}
}
