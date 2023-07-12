/*
 * Copyright 2023 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.ioc.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 */
@Target({ METHOD })
@Retention(RUNTIME)
@Documented
public @interface InterceptBeforeCall {
	/**
	 * The value can be either
	 * <p>
	 * a) the method name of the method in the {@code InterceptionController},
	 * <p>
	 * b) a class name followed by "::" followed by a method name of a static method,
	 * <p>
	 * 
	 * the specified method must have an method signature like:
	 * {@code public Optional<Object> method(Object, Method, Object[])}.
	 * 
	 * @return the value
	 */
	String value();
}