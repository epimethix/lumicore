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
 * Configure the database metadata.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaMetadata {
	/**
	 * Default value: {@value #UNIQUE_DEFAULT}
	 */
	public static final boolean UNIQUE_DEFAULT = false;
	/**
	 * Default value: {@value #INIT_METADATA_DEFAULT}
	 */
	public static final boolean INIT_METADATA_DEFAULT = false;

	/**
	 * set to true to generate an UUID when initially deploying the database.
	 * 
	 * @return {@value #UNIQUE_DEFAULT} by default
	 * 
	 */
	boolean unique() default UNIQUE_DEFAULT;

	/**
	 * set to true to initialize database metadata time stamps with
	 * {@link System#currentTimeMillis()}.
	 * 
	 * @return {@value #INIT_METADATA_DEFAULT} by default
	 * 
	 */
	boolean initializeTimeStamps() default INIT_METADATA_DEFAULT;
}
