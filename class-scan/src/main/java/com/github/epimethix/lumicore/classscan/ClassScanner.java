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

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Interface to specify a class scanner.
 * <p>
 * A class scanner has two main tasks. A) scan and index classes, B) search the
 * index for classes.
 * <p>
 * if a search specifies a package name to inspect that was not yet scanned, the
 * implementation of the search should trigger a scan.
 * 
 * @author epimethix
 *
 */
public interface ClassScanner {
	/**
	 * Searches for classes that pass the check
	 * {@code cls.isAssignableFrom(classToTest) == true}, limited to the specified
	 * package name (and its sub packages).
	 * <p>
	 * If the specified package name was not scanned yet then the implementation of
	 * this method should trigger a scan for the package.
	 * 
	 * @param packageName the package name to limit the search to; "*" to search all
	 *                    indexed classes.
	 * @param cls         the interface or class to which classes to select must be
	 *                    assignable.
	 * @return the result of the search
	 */
	Collection<Class<?>> searchClassesAssignableFrom(String packageName, Class<?> cls);

	/**
	 * Searches the index for classes that pass the check
	 * {@code cls.isAssignableFrom(classToTest) == true}.
	 * 
	 * @param cls the interface or class to which classes to select must be
	 *            assignable.
	 * @return the result of the search
	 */
	Collection<Class<?>> searchClassesAssignableFrom(Class<?> cls);

	/**
	 * Searches the index for classes that pass the check
	 * {@code classToTest.isAnnotationPresent(annotation) == true}, limited to the
	 * specified package (and its sub packages).
	 * 
	 * @param packageName the package name to limit the search to; "*" to search all
	 *                    indexed classes.
	 * @param annotation  the annotation which must be present to select a class
	 * @return the result of the search
	 */
	Collection<Class<?>> searchClassesByAnnotation(String packageName, Class<? extends Annotation> annotation);

	/**
	 * Searches the index for classes that pass the check
	 * {@code classToTest.isAnnotationPresent(annotation) == true}.
	 * 
	 * @param annotation the annotation which must be present to select a class
	 * @return the result of the search
	 */
	Collection<Class<?>> searchClassesByAnnotation(Class<? extends Annotation> annotation);
}
