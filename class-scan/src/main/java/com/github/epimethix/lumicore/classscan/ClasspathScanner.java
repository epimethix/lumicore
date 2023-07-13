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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.epimethix.lumicore.benchmark.Benchmark;
import com.github.epimethix.lumicore.benchmark.Benchmark.Check;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;

/**
 * 
 * @author epimethix
 *
 */
public class ClasspathScanner implements ClassScanner {

	private final static Logger LOGGER = Log.getLogger(Log.CHANNEL_IOC);

	private final Map<String, List<Class<?>>> classCache;

	private final ClassCriteria scanCriteria;

	public ClasspathScanner() {
		this(new String[] {});
	}

	public ClasspathScanner(String packageName) {
		this(packageName, cls -> true);
	}

	public ClasspathScanner(String packageName, ClassCriteria scanCriteria) {
		this(new String[] { packageName }, scanCriteria);
	}

	public ClasspathScanner(String[] packageNames) {
		this(packageNames, cls -> true);
	}

	public ClasspathScanner(String[] packageNames, ClassCriteria scanCriteria) {
		this.scanCriteria = scanCriteria;
		classCache = new HashMap<>();
		for (String packageName : packageNames) {
			scan(packageName);
		}
	}

	private final void scan(String packageName) {
		for (String knownPackage : classCache.keySet()) {
			if (packageName.startsWith(knownPackage)) {
				return;
			}
		}
		List<Class<?>> result = scan(packageName, scanCriteria);
		classCache.put(packageName, result);
	}

	private final List<Class<?>> scan(String packageName, ClassCriteria criteria) {
//		System.out.println(Profile.getRunningDirectory());
		Check ckScan = Benchmark.start(ClasspathScanner.class, "scan(" + packageName + ")", "ClasspathScanner.scan");
		List<Class<?>> result = new ArrayList<>();
		try {
			scan(packageName, criteria, result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ckScan.stop();
		return result;
	}

	private final void scan(String packageName, ClassCriteria criteria, List<Class<?>> result) throws IOException {
		if (Objects.nonNull(classCache.get(packageName))) {
			return;
		}
		String resourceName = packageName.replaceAll("[.]", "/");
		try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceName)) {
//			System.out.printf("Scanner: inputstream to '%s' is null: %b%n", resourceName, Objects.isNull(is));
			if (Objects.nonNull(is)) {
				try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
					String line;
					while (Objects.nonNull(line = br.readLine())) {
//						System.out.println(line);
						Class<?> cls = loadClass(packageName, line);
						if (Objects.nonNull(cls)) {
							if (criteria.accept(cls)) {
//								System.out.println("+add class " + cls.getSimpleName());
								result.add(cls);
							}
						} else if (!line.contains(".")) {
							scan(String.format("%s.%s", packageName, line), criteria, result);
						}
					}
				}
			}
		}
	}

	private Class<?> loadClass(String packageName, String typeName) {
		if (!typeName.endsWith(".class") || typeName.contains("$")) {
			return null;
		}
		Class<?> cls = null;
		typeName = typeName.substring(0, typeName.lastIndexOf(".class"));
		try {
			cls = Class.forName(String.format("%s.%s", packageName, typeName));
		} catch (ClassNotFoundException e) {}
		return cls;
	}

	public final List<Class<?>> search(ClassCriteria criteria) {
		return search("*", criteria);
	}

	public final List<Class<?>> search(String packageName, ClassCriteria criteria) {
		return search(packageName, criteria, true);
	}

	public final List<Class<?>> search(String packageName, ClassCriteria criteria, boolean recursive) {
//		long startTime = System.currentTimeMillis();
//		long nanoTime = System.nanoTime();
		List<Class<?>> source = null;
		Set<String> keys = classCache.keySet();
		List<Class<?>> result = new ArrayList<>();
		boolean searchAll = packageName.equals("*");
		for (String key : keys) {
			if (packageName.startsWith(key) || searchAll) {
				source = classCache.get(key);
				searchAndAdd(packageName, criteria, recursive, searchAll, source, result);
			}
		}
		if (Objects.isNull(source) && !searchAll) {
			source = scan(packageName, scanCriteria);
			classCache.put(packageName, source);
			searchAndAdd(packageName, criteria, recursive, searchAll, source, result);
		}
//		if (LumicoreProperties.IOC_VERBOSE) {
//			System.out.printf("### Scanner: search took %,d millis / %,d nanos%n",
//					System.currentTimeMillis() - startTime, System.nanoTime() - nanoTime);
//		}
		return result;
	}

	private void searchAndAdd(String packageName, ClassCriteria criteria, boolean recursive, boolean searchAll,
			List<Class<?>> source, List<Class<?>> result) {
		for (Class<?> cls : source) {
			String pkgName = cls.getPackageName();
			boolean condition;
			if(!searchAll) {
				condition = recursive ? pkgName.startsWith(packageName) : pkgName.equals(packageName);
			} else {
				condition = true;
			}
			if (condition && criteria.accept(cls)) {
				result.add(cls);
			}
		}
	}

//### Scanning took 178 millis
//### Scanner: search took 17 millis / 16,979,723 nanos
//### Scanner: search took 0 millis / 24,891 nanos
//### Scanner: search took 0 millis / 32,071 nanos

//### Scanning took 229 millis
//### Scanner: search took 23 millis / 22,671,948 nanos
//### Scanner: search took 0 millis / 22,845 nanos
//### Scanner: search took 0 millis / 23,018 nanos
	@SuppressWarnings("unused")
	private void searchAndAdd2(String packageName, ClassCriteria criteria, boolean recursive, boolean searchAll,
			List<Class<?>> source, List<Class<?>> result) {
		for (Class<?> cls : source) {
			if ((searchAll || (recursive ? cls.getPackageName().startsWith(packageName)
					: cls.getPackageName().equals(packageName))) && criteria.accept(cls)) {
				result.add(cls);
			}
		}

	}

	@Override
	public Collection<Class<?>> searchClassesByAnnotation(Class<? extends Annotation> annotation) {
		return searchClassesByAnnotation("*", annotation);
	}

	@Override
	public Collection<Class<?>> searchClassesByAnnotation(String packageName, Class<? extends Annotation> annotation) {

		Check ckSearch = Benchmark.start(ClasspathScanner.class,
				"searchClassesByAnnotation(" + annotation.getSimpleName() + ")", "ClassPathScanner.search");
		Collection<Class<?>> result = search(packageName, (cls) -> cls.isAnnotationPresent(annotation), true);
		ckSearch.stop();
		LOGGER.trace("searchClassesByAnnotation(%s, %s) returned %d results", packageName, annotation.getName(),
				result.size());
		return result;
	}

	@Override
	public Collection<Class<?>> searchClassesAssignableFrom(Class<?> cls) {
		return searchClassesAssignableFrom("*", cls);
	}

	@Override
	public Collection<Class<?>> searchClassesAssignableFrom(String packageName, Class<?> cls) {
		Check ckSearch = Benchmark.start(ClasspathScanner.class,
				"searchClassesAssignableFrom(" + cls.getSimpleName() + ")", "ClassPathScanner.search");
		Collection<Class<?>> result = search(packageName, (c) -> cls.isAssignableFrom(c), true);
		ckSearch.stop();
		LOGGER.trace("searchClassesAssignableFrom(%s, %s) returned %d results", packageName, cls.getName(),
				result.size());
		return result;
	}

	@Override
	public Collection<Class<?>> searchClassesByCriteria(ClassCriteria criteria) {
		return searchClassesByCriteria("*", criteria);
	}

	@Override
	public Collection<Class<?>> searchClassesByCriteria(String packageName, ClassCriteria criteria) {
		Check ckSearch = Benchmark.start(ClasspathScanner.class,
				"searchClassesByCriteria(" + packageName + ")", "ClassPathScanner.search");
		Collection<Class<?>> result = search(packageName, criteria, true);
		ckSearch.stop();
		LOGGER.trace("searchClassesByCriteria(%s) returned %d results", packageName,
				result.size());
		return result;
	}
}
