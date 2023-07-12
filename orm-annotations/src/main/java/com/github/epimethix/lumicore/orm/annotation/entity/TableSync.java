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
package com.github.epimethix.lumicore.orm.annotation.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.epimethix.lumicore.orm.annotation.database.SchemaSync;

/**
 * Configure the auto sync process for an Entity.
 * <p>
 * Apply to a class implementing Entity.
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableSync {
	/**
	 * @return {@value SchemaSync#DEPLOY_NEW_COLUMNS_DEFAULT} by default
	 */
	boolean deployNewColumns() default SchemaSync.DEPLOY_NEW_COLUMNS_DEFAULT;

	/**
	 * @return {@value SchemaSync#DROP_COLUMNS_DEFAULT} by default
	 * 
	 */
	boolean dropColumns() default SchemaSync.DROP_COLUMNS_DEFAULT;

	/**
	 * @return {@value SchemaSync#REDEFINE_ENTITY_DEFAULT} by default
	 */
	boolean redefineEntity() default SchemaSync.REDEFINE_ENTITY_DEFAULT;
}
