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
 * Intercept method call and perform access check.
 * 
 */
@Target({ METHOD })
@Retention(RUNTIME)
@Documented
public @interface InterceptAllowCaller {
	/**
	 * 
	 * The value can be either
	 * <p>
	 * a) a class name followed by "::" followed by a method name to allow calls from a specific method,
	 * <p>
	 * b) a class name followed by "::" to allow all calls from a class,
	 * <p>
	 * c) a package name followed by "." to allow all calls from all classes in the package,
	 * <p>
	 * d) a package name to allow all calls from all sub classes of the package and sub packages,
	 * <p>
	 * 
	 * @return the value
	 */
	String value();
	
	/**
	 * 
	 * 
	 * @return the allowed intermediate callers
	 */
	String[] intermediate() default {};
}