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
package com.github.epimethix.lumicore.orm.annotation.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EntityDefinition is used to specify some table name other than the entity
 * class simple name and to enable logging, soft delete and without rowid.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
	/**
	 * Default value: {@value #NAME_DEFAULT}
	 */
	public static final String NAME_DEFAULT = "";
	/**
	 * Value to disable logging
	 * 
	 * @see #logging()
	 */
	public static final int DISABLE_LOGGING = 1;
	/**
	 * Value to enable logging (CRUD events)
	 * 
	 * @see #logging()
	 */
	public static final int ENABLE_LOGGING_CRUD = 2;
//	/**
//	 * Value to enable logging (CRUD+Listing events)
//	 * 
//	 * @see #logging()
//	 */
//	public static final int ENABLE_LOGGING_CRUDL = 3;
	/**
	 * Default value: {@value #LOGGING_DEFAULT} (DISABLE_LOGGING)
	 */
	public static final int LOGGING_DEFAULT = DISABLE_LOGGING;
	/**
	 * Default value: {@value #ENABLE_SOFT_DELETE_DEFAULT}
	 */
	public static final boolean ENABLE_SOFT_DELETE_DEFAULT = false;
	/**
	 * Default value: {@value #WITHOUT_ROWID_DEFAULT}
	 */
	public static final boolean WITHOUT_ROWID_DEFAULT = false;
	
	public static final boolean STRICT_DEFAULT = false;

	public static final int STRATEGY_IMPLICIT_DEFINITION = 1;
	public static final int STRATEGY_EXPLICIT_DEFINITION = 2;
	public static final int STRATEGY_DEFAULT = STRATEGY_IMPLICIT_DEFINITION;

	/**
	 * Set the name to override the default entity simple class name.
	 * 
	 * @return {@value #NAME_DEFAULT} by default
	 */
	String name() default NAME_DEFAULT;

	/**
	 * Enable logging in separate event log table.
	 * 
	 * @return {@value #LOGGING_DEFAULT} (DISABLE_LOGGING) by default
	 */
	int logging() default LOGGING_DEFAULT;

	/**
	 * Enable soft delete (only mark the record delete date and filter all non zero
	 * delete time records from selections; requires an entity class that extends
	 * {@code AbstractMetaEntity}.
	 * 
	 * @return {@value #ENABLE_SOFT_DELETE_DEFAULT} by default
	 */
	boolean enableSoftDelete() default ENABLE_SOFT_DELETE_DEFAULT;

	/**
	 * append WITHOUT ROWID to the create statement.
	 * 
	 * @return {@value #WITHOUT_ROWID_DEFAULT} by default
	 */
	boolean withoutRowID() default WITHOUT_ROWID_DEFAULT;
	
	boolean strict() default STRICT_DEFAULT;

	/**
	 * define strategy for ORM picking up fields from the entity class.
	 * 
	 * <ul>
	 * <li>{@link Table#STRATEGY_IMPLICIT_DEFINITION} (default) :
	 * {@code @FieldDefinition} is optional, all instance fields are automatically
	 * picked up as database fields.
	 * <li>{@link Table#STRATEGY_EXPLICIT_DEFINITION} :
	 * {@code @FieldDefinition} is required to explicitly define all database table
	 * fields.
	 * </ul>
	 * 
	 * @return {@link Table#STRATEGY_IMPLICIT_DEFINITION} by default
	 */
	int strategy() default STRATEGY_DEFAULT;
}
