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
package com.github.epimethix.lumicore.orm.annotation.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne {
	String fieldName() default "";

	String referencedFieldName() default "";

	boolean lazy() default false;

	/**
	 * Configure nullability of the field.
	 * 
	 * @return Definition.NULLABLE by default.
	 * 
	 */
	int nullHandling() default Column.NULL_HANDLING_DEFAULT;

	/**
	 * Set a default value.
	 * 
	 * @return Definition.NO_DEFAULT by default.
	 * 
	 */
	int defaultHandling() default Column.DEFAULT_HANDLING_DEFAULT;

	/**
	 * the default value as String. set this value in combination with
	 * {@code defaultHandling = Definition.DEFAULT_AS_SPECIFIED}.
	 * 
	 * @return {@value Column#DEFAULT_VALUE_DEFAULT} by default.
	 */
	String defaultValue() default Column.DEFAULT_VALUE_DEFAULT;

	/**
	 * Set a check constraint for the field.
	 * 
	 * @return {@value Column#CHECK_CONSTRAINT_DEFAULT} by default.
	 */
	String check() default Column.CHECK_CONSTRAINT_DEFAULT;

//	boolean passive() default false;
}
