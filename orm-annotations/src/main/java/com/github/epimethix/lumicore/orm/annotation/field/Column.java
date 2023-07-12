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
package com.github.epimethix.lumicore.orm.annotation.field;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FieldDefinition is applied to an {@code Entity<T>} field. This way the
 * generator picks up table fields. If no other name is specified the java field
 * name is used for naming the table field. But this is discouraged anyhow... to
 * define primary key fields the type must be specified explicitly.
 * 
 * @author epimethix
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	/**
	 * Default value: {@value #NAME_DEFAULT}
	 */
	public static final String NAME_DEFAULT = "";
	/**
	 */
	public static final int TYPE_DEFAULT = 10;
	/**
	 */
	public static final int NULL_HANDLING_DEFAULT = 1;
	/**
	 * Default value: {@value #DEFAULT_HANDLING_DEFAULT}
	 */
	public static final int DEFAULT_HANDLING_DEFAULT = 1;
	/**
	 * Default value: {@value #DEFAULT_VALUE_DEFAULT}
	 */
	public static final String DEFAULT_VALUE_DEFAULT = "";
	/**
	 * Default value: {@value #CHECK_CONSTRAINT_DEFAULT}
	 */
	public static final String CHECK_CONSTRAINT_DEFAULT = "";
	
	public static final Column DEFAULT_DEFINITION = new Column() {
		
		@Override
		public Class<? extends Annotation> annotationType() {
			return getClass();
		}
		
		@Override
		public String name() {
			return Column.NAME_DEFAULT;
		}

		@Override
		public int type() {
			return Column.TYPE_DEFAULT;
		}

		@Override
		public int nullHandling() {
			return Column.NULL_HANDLING_DEFAULT;
		}

		@Override
		public int defaultHandling() {
			return Column.DEFAULT_HANDLING_DEFAULT;
		}

		@Override
		public String defaultValue() {
			return Column.DEFAULT_VALUE_DEFAULT;
		}

		@Override
		public String check() {
			return Column.CHECK_CONSTRAINT_DEFAULT;
		}
	};

	/**
	 * The field name to use in SQL.
	 * 
	 * @return {@value #NAME_DEFAULT} by default.
	 */
	String name() default "";

	/**
	 * Use either one of the available types in Definition
	 * 
	 * @return Definition.TYPE_AUTO by
	 *         default.
	 */
	int type() default TYPE_DEFAULT;

	/**
	 * Configure nullability of the field.
	 * 
	 * @return Definition.NULLABLE by
	 *         default.
	 * 
	 */
	int nullHandling() default NULL_HANDLING_DEFAULT;

	/**
	 * Set a default value.
	 * 
	 * @return Definition.NO_DEFAULT by
	 *         default.
	 * 
	 */
	int defaultHandling() default DEFAULT_HANDLING_DEFAULT;

	/**
	 * the default value as String. set this value in combination with
	 * {@code defaultHandling = Definition.DEFAULT_AS_SPECIFIED}.
	 * 
	 * @return {@value #DEFAULT_VALUE_DEFAULT} by default.
	 */
	String defaultValue() default DEFAULT_VALUE_DEFAULT;

	/**
	 * Set a check constraint for the field.
	 * 
	 * @return {@value #CHECK_CONSTRAINT_DEFAULT} by default.
	 */
	String check() default CHECK_CONSTRAINT_DEFAULT;
}
