/*
 * Copyright 2021 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.orm.annotation.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configure the automatic synchronization process.
 * <p>
 * Apply to an implementation class of {@code AbstractDatabase}.
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaSync {
	/**
	 * Default value: {@value #DEPLOY_NEW_TABLES_DEFAULT}
	 */
	public static final boolean DEPLOY_NEW_TABLES_DEFAULT = true;
	/**
	 * Default value: {@value #DEPLOY_NEW_COLUMNS_DEFAULT}
	 */
	public static final boolean DEPLOY_NEW_COLUMNS_DEFAULT = true;
	/**
	 * Default value: {@value #DROP_TABLES_DEFAULT}
	 */
	public static final boolean DROP_TABLES_DEFAULT = false;
	/**
	 * Default value: {@value #DROP_COLUMNS_DEFAULT}
	 */
	public static final boolean DROP_COLUMNS_DEFAULT = true;
	/**
	 * Default value: {@value #UPGRADE_SCHEMA_DEFAULT}
	 */
	public static final boolean UPGRADE_SCHEMA_DEFAULT = true;
	/**
	 * Default value: {@value #REDEFINE_ENTITY_DEFAULT}
	 */
	public static final boolean REDEFINE_ENTITY_DEFAULT = true;

	/**
	 * @return {@value #DEPLOY_NEW_TABLES_DEFAULT} by default
	 */
	boolean deployNewTables() default DEPLOY_NEW_TABLES_DEFAULT;

	/**
	 * @return {@value #DEPLOY_NEW_COLUMNS_DEFAULT} by default
	 */
	boolean deployNewColumns() default DEPLOY_NEW_COLUMNS_DEFAULT;

	/**
	 * @return {@value #DROP_TABLES_DEFAULT} by default
	 * 
	 */
	boolean dropTables() default DROP_TABLES_DEFAULT;

	/**
	 * @return {@value #DROP_COLUMNS_DEFAULT} by default
	 * 
	 */
	boolean dropColumns() default DROP_COLUMNS_DEFAULT;

	/**
	 * @return {@value #UPGRADE_SCHEMA_DEFAULT} by default
	 */
	boolean upgradeSchema() default UPGRADE_SCHEMA_DEFAULT;

	/**
	 * @return {@value #REDEFINE_ENTITY_DEFAULT} by default
	 */
	boolean redefineEntity() default REDEFINE_ENTITY_DEFAULT;
}
