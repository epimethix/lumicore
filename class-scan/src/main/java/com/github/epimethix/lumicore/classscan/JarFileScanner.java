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

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import com.github.epimethix.lumicore.benchmark.Benchmark;
import com.github.epimethix.lumicore.benchmark.Benchmark.Check;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;

public class JarFileScanner implements ClassScanner {

	private final static Logger LOGGER = Log.getLogger(Log.CHANNEL_IOC);

	private final Map<String, List<Class<?>>> classCache;

	private final ClassCriteria scanCriteria;

	private final String jarFilePath;

	public JarFileScanner(String jarFilePath, String packageName) {
		this(jarFilePath, new String[] { packageName });
	}

	public JarFileScanner(String jarFilePath, String[] packages) {
		this(jarFilePath, packages, (cls) -> true);
	}

	public JarFileScanner(String jarFilePath, String packageName, ClassCriteria scanCriteria) {
		this(jarFilePath, new String[] { packageName }, scanCriteria);
	}

	public JarFileScanner(String jarFilePath, String[] packages, ClassCriteria scanCriteria) {
		this.jarFilePath = jarFilePath;
		this.scanCriteria = scanCriteria;
		this.classCache = new HashMap<>();
		for (String packageName : packages) {
			scan(packageName);
		}
		LOGGER.info("Scanner was initialized for: %s", jarFilePath);
	}

	private void scan(String packageName) {
		Check ckScan = Benchmark.start(JarFileScanner.class, "scan(" + packageName + ")", "JarFileScanner.scan");
		File[] filesToSearch;
		File inputFile = new File(jarFilePath);
		if (inputFile.isDirectory()) {
			filesToSearch = inputFile.listFiles((f) -> f.isFile() && f.getName().endsWith(".jar"));
		} else {
			filesToSearch = new File[] { inputFile };
		}
		Pattern validClassNamePattern = Pattern.compile("[^$]+\\.class$");
		List<Class<?>> classes = new ArrayList<>();
		for (File file : filesToSearch) {
			try (JarFile jarFile = new JarFile(file)) {
				Enumeration<JarEntry> e = jarFile.entries();
				while (e.hasMoreElements()) {
					JarEntry jarEntry = e.nextElement();
					String jarEntryName = jarEntry.getName();
					if (validClassNamePattern.matcher(jarEntryName).matches()) {
						String className = jarEntryName.replace("/", ".").replaceAll("\\.class$", "");
						if (className.startsWith(packageName)) {
							try {
								Class<?> cls = Class.forName(className);
								if (scanCriteria.accept(cls)) {
//									System.out.println(className);
									classes.add(cls);
								}
							} catch (ClassNotFoundException e1) {
//								e1.printStackTrace();
								LOGGER.error(e1);
							}
						}
					}
				}
			} catch (IOException e) {
				LOGGER.error(e);
//				e.printStackTrace();
			}
		}
		classCache.put(packageName, classes);
		ckScan.stop();
	}

	public final Collection<Class<?>> search(ClassCriteria criteria) {
		return search("*", criteria);
	}

	public final Collection<Class<?>> search(String packageName, ClassCriteria criteria) {
		Set<String> keys = classCache.keySet();
		boolean wasInScope = false;
		Collection<Class<?>> result = new ArrayList<>();
		for (String key : keys) {
			if (packageName.equals("*") || packageName.startsWith(key)) {
				if (!wasInScope) {
					wasInScope = true;
				}
				List<Class<?>> classes = classCache.get(key);
				for (Class<?> cls : classes) {
					if (packageName.equals("*") || cls.getName().startsWith(packageName)) {
						if (criteria.accept(cls)) {
							result.add(cls);
						}
					}
				}
			}
		}
		if (!wasInScope) {
//			scan(packageName);
		}
		return result;
	}

	@Override
	public Collection<Class<?>> searchClassesAssignableFrom(String packageName, Class<?> cls) {
		Check ckSearch = Benchmark.start(JarFileScanner.class,
				"searchClassesAssignableFrom(" + cls.getSimpleName() + ")", "JarFileScanner.search");
		Collection<Class<?>> result = search(packageName, (c) -> cls.isAssignableFrom(c));
		ckSearch.stop();
		LOGGER.trace("searchClassesAssignableFrom(%s, %s) returned %d results", packageName, cls.getName(),
				result.size());
		return result;
	}

	@Override
	public Collection<Class<?>> searchClassesAssignableFrom(Class<?> cls) {
		return searchClassesAssignableFrom("*", cls);
	}

	@Override
	public Collection<Class<?>> searchClassesByAnnotation(String packageName, Class<? extends Annotation> annotation) {
		Check ckSearch = Benchmark.start(JarFileScanner.class,
				"searchClassesByAnnotation(" + annotation.getSimpleName() + ")", "JarFileScanner.search");
		Collection<Class<?>> result = search(packageName, (cls) -> cls.isAnnotationPresent(annotation));
		ckSearch.stop();
		LOGGER.trace("searchClassesByAnnotation(%s, %s) returned %d results", packageName, annotation.getName(),
				result.size());
		return result;
	}

	@Override
	public Collection<Class<?>> searchClassesByAnnotation(Class<? extends Annotation> annotation) {
		return searchClassesByAnnotation("*", annotation);
	}

}
