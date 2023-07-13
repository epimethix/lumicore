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
package com.github.epimethix.lumicore.classscan;

/**
 * Functional interface to specify a class scan/search criterion.
 * <p>
 * This interface is used by some implementations of {@link ClassScanner}.
 * 
 * @author epimethix
 *
 */
@FunctionalInterface
public interface ClassCriteria {
	/**
	 * The implementation should return true to select the specified class.
	 * 
	 * @param cls the class to inspect
	 * @return true to select the class
	 */
	boolean accept(Class<?> cls);
}