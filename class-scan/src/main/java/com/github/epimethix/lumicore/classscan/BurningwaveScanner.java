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

public class BurningwaveScanner{// implements Scanner {
/*
	private static Collection<Class<?>> getClassesAssignableFrom(String packageName, final Class<?> cls) {
		// @formatter:off
		return getClassesByCriteria(packageName,
				ClassCriteria
				.create()
				.byClassesThatMatch(
						(uploadedClasses, currentScannedClass) -> 
							uploadedClasses.get(cls).isAssignableFrom(currentScannedClass)
					)
				.useClasses(cls)
			);
        // @formatter:on
	}

	private static Collection<Class<?>> getClassesByAnnotation(String packageName,
			final Class<? extends Annotation> annotation) {
		// @formatter:off
		return getClassesByCriteria(packageName,
				ClassCriteria
				.create()
				.allThoseThatMatch(cls -> cls.isAnnotationPresent(annotation))
			);
        // @formatter:on
	}

	private static Collection<Class<?>> getClassesByCriteria(String packageName, final ClassCriteria classCriteria) {
		long time = System.currentTimeMillis();
		ComponentSupplier componentSupplier = ComponentContainer.getInstance();
		ClassHunter classHunter = componentSupplier.getClassHunter();
		// @formatter:off
		SearchConfig searchConfig = SearchConfig
				.forResources(packageName.replace('.', '/'))
				.by(classCriteria);
		// @formatter:on		

		try (ClassHunter.SearchResult searchResult = classHunter.findBy(searchConfig)) {
			return searchResult.getClasses();
		} finally {
			System.out.printf("### Burningwave Scanner: Scanning took %,d millis%n", System.currentTimeMillis() - time);
		}
	}

	private final String[] packages;

	public BurningwaveScanner(String pkg) {
		this(new String[] { pkg });
	}

	public BurningwaveScanner(String[] packages) {
		this.packages = packages;
	}

	@Override
	public Collection<Class<?>> searchClassesAssignableFrom(String packageName, Class<?> cls) {
		return getClassesAssignableFrom(packageName, cls);
	}

	@Override
	public Collection<Class<?>> searchClassesAssignableFrom(Class<?> cls) {
		Collection<Class<?>> result = getClassesAssignableFrom(packages[0], cls);
		if (packages.length > 1) {
			for (int i = 1; i < packages.length; i++) {
				result.addAll(getClassesAssignableFrom(packages[i], cls));
			}
		}
		return result;
	}

	@Override
	public Collection<Class<?>> searchClassesByAnnotation(String packageName, Class<? extends Annotation> annotation) {
		return getClassesByAnnotation(packageName, annotation);
	}

	@Override
	public Collection<Class<?>> searchClassesByAnnotation(Class<? extends Annotation> annotation) {
		Collection<Class<?>> result = getClassesByAnnotation(packages[0], annotation);
		if (packages.length > 1) {
			for (int i = 1; i < packages.length; i++) {
				result.addAll(getClassesByAnnotation(packages[i], annotation));
			}
		}
		return result;
	}
*/
}
