/*
 * Copyright 2022 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.common.orm;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for transporting the state of synchronizity between the
 * Java/application definition and the database file (field level).
 */
public final class EntitySynchronizity {
	/**
	 * the fields to create / fields that exist in the application definition but
	 * not the database file
	 */
	private final List<String> fieldsToCreate;
	/**
	 * The fields existing in application definition and the database file
	 * synchronously
	 */
	private final List<String> fieldsToCheck;
	/**
	 * The fields that exist in the database file but not in the application
	 * definition
	 */
	private final List<String> fieldsToDelete;

	public EntitySynchronizity() {
		this.fieldsToCreate = new ArrayList<>();
		this.fieldsToCheck = new ArrayList<>();
		this.fieldsToDelete = new ArrayList<>();
	}

	/**
	 * get the list of field names to create / that are not deployed in the current
	 * database file yet.
	 * 
	 * @return the fields to create.
	 */
	public List<String> getFieldsToCreate() {
		return fieldsToCreate;
	}

	/**
	 * get the field names that simultaneously exist in application definition and
	 * database file.
	 * 
	 * @return the fields to inspect closer.
	 */
	public List<String> getFieldsToCheck() {
		return fieldsToCheck;
	}

	/**
	 * get the fields that exist in the database file but not in the application
	 * definition.
	 * 
	 * @return the fields to delete.
	 */
	public List<String> getFieldsToDelete() {
		return fieldsToDelete;
	}
}
