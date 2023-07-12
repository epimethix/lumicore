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
package com.github.epimethix.lumicore.orm.model.i;

import com.github.epimethix.lumicore.common.orm.model.Entity;

/**
 * implementing {@code MetaEntity<T>} will make {@code AbstractRepository<T, U>}
 * track record based meta data.
 * 
 * @author epimethix
 * 
 * @param <ID> the entities id (primary key) type
 */
public interface MetaEntity<ID> extends Entity<ID> {
	/**
	 * Column name: {@value #CREATE_DATE}
	 */
	public final static String CREATE_DATE = "createDate";
	/**
	 * Column name: {@value #READ_DATE}
	 */
	public final static String READ_DATE = "readDate";
	/**
	 * Column name: {@value #UPDATE_DATE}
	 */
	public final static String UPDATE_DATE = "updateDate";
	/**
	 * Column name: {@value #DELETE_DATE}
	 */
	public final static String DELETE_DATE = "deleteDate";

	/**
	 * Gets the create date of the record
	 * 
	 * @return the create date
	 */
	long getCreateDate();

	/**
	 * Gets the read date of the record
	 * 
	 * @return the read date
	 */
	long getReadDate();

	/**
	 * Gets the update date of the record
	 * 
	 * @return the update date
	 */
	long getUpdateDate();

	/**
	 * Gets the delete date of the record
	 * 
	 * @return the delete date
	 */
	long getDeleteDate();
}
