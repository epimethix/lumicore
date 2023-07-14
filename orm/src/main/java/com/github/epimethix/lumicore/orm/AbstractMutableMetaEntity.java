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
 * Extending {@link AbstractMutableMetaEntity} this entity class enables record meta
 * data tracking by {@link SQLRepository}.
 * 
 * @param <ID> the unique identifier type of the {@code Entity<T>}
 *             implementation
 * 
 * @see MetaEntity
 */
public abstract class AbstractMutableMetaEntity<ID> implements MetaEntity<ID> {
	// @formatter:off
	private static final AccessCheck AC_ABSTRACT_REPOSITORY = AccessCheck.Builder.createAllowAbstractRepositoryViaReflection();
	// @formatter:on

	@Column(name = CREATE_DATE)
	private long createDate;

	@Column(name = READ_DATE)
	private long readDate;

	@Column(name = UPDATE_DATE)
	private long updateDate;

	@Column(name = DELETE_DATE)
	private long deleteDate;

	public AbstractMutableMetaEntity() {}

	@Override
	public final long getCreateDate() {
		return createDate;
	}

	final void setCreateDate(long date) throws IllegalAccessException {
		AC_ABSTRACT_REPOSITORY.checkPermission();
		this.createDate = date;
	}

	@Override
	public final long getReadDate() {
		return readDate;
	}

	final void setReadDate(long date) throws IllegalAccessException {
		AC_ABSTRACT_REPOSITORY.checkPermission();
		this.readDate = date;
	}

	@Override
	public final long getUpdateDate() {
		return updateDate;
	}

	final void setUpdateDate(long date) throws IllegalAccessException {
		AC_ABSTRACT_REPOSITORY.checkPermission();
		this.updateDate = date;
	}

	@Override
	public final long getDeleteDate() {
		return deleteDate;
	}

	final void setDeleteDate(long deleteDate) throws IllegalAccessException {
		AC_ABSTRACT_REPOSITORY.checkPermission();
		this.deleteDate = deleteDate;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return true;
	}
}
