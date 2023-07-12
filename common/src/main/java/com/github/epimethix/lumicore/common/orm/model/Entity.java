/*
 * Copyright 2021-2023 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.common.orm.model;

import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.orm.annotation.entity.ImplementationClass;
import com.github.epimethix.lumicore.orm.annotation.entity.Table;

/**
 * The {@code Entity<ID>} interface is the contract every database entity has to
 * fulfill.
 * <p>
 * The implementation class needs to specify a field called 'id'. The getter and
 * setter for this field are specified in the {@code Entity<ID>}. The type of
 * the field 'id' is supposed to be the interface type parameter ID.
 * 
 * @author epimethix
 *
 * @param <ID> the type of the field id (primary key).
 * 
 */
public interface Entity<ID> {
	/**
	 * The STRUCTURE_VERSION field should be hidden by the implementation class to
	 * indicate the entities structural version.
	 * <p>
	 * the structure version is tracked; changing the structure version to a greater
	 * value should trigger the upgrade process.
	 * 
	 * @see Repository#upgrade(long)
	 */
	public static long STRUCTURE_VERSION = 0L;

//	default long getEntityStructureVersion() {
//		return 0L;
//	}
	/**
	 * Column name: '{@value #ID}' for optional use
	 */
	public static final String ID = "id";

	/**
	 * Gets the unique identifier of the record
	 * 
	 * @return the records id
	 */
	public ID getId();

//	public EntityBuilder<ID> getBuilder();

	/**
	 * Checks if this record has an ID.
	 * 
	 * @return true if {@link Entity#getId()} returns null
	 */
	public default boolean isNew() {
		return Objects.isNull(getId());
	}

	public interface EntityBuilder<ID> {

		/**
		 * Sets the unique identifier of the record
		 * 
		 * @param id the id
		 */
		public EntityBuilder<ID> setId(ID id);

		public Entity<ID> build();
	}

	/**
	 * Gets the corresponding entity name considering the @EntityDefinition
	 * annotation.
	 * 
	 * @param entityClass the entity class
	 * 
	 * @return the simpleClassName() of the entity class if no @EntityDefinition is
	 *         available; otherwise the defined name from the @EntityDefinition
	 *         annotation.
	 */
	public static String getEntityName(Class<? extends Entity<?>> entityClass) {
		if (entityClass.isAnnotationPresent(ImplementationClass.class)) {
			entityClass = (Class<? extends Entity<?>>) entityClass.getAnnotation(ImplementationClass.class).value();
		}
		Table ed = entityClass.getAnnotation(Table.class);
		if (Objects.nonNull(ed) && Objects.nonNull(ed.name()) && !ed.name().isEmpty()) {
			return ed.name();
		} else {
			return entityClass.getSimpleName();
		}
	}

}
