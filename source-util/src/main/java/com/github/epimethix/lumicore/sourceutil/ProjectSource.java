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
package com.github.epimethix.lumicore.sourceutil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProjectSource {
	public static final ProjectSource scanSources(File srcDir) {
		return scan(srcDir, ".java");
	}

	public static final ProjectSource scan(File srcDir, String extension) {
		StringBuilder basePackageBuilder = new StringBuilder();

		File[] ls = srcDir.listFiles();
		File basePackageFile = srcDir;
		boolean firsRound = true;
		while (Objects.nonNull(ls) && ls.length == 1 && ls[0].isDirectory()) {
			if (!firsRound) {
				basePackageBuilder.append(".");
				firsRound = false;
			}
			basePackageBuilder.append(ls[0].getName());
			basePackageFile = ls[0];
			ls = ls[0].listFiles();
//			if (firsRound) {
//				firsRound = false;
//			}
		}
		String basePackage = basePackageBuilder.toString();

		List<String> packages = new ArrayList<>();
		Map<String, List<String>> contents = new HashMap<>();
		scan(srcDir, basePackageFile, packages, contents, extension);

		return new ProjectSource(srcDir, basePackage, packages, contents);
	}

	private static void scan(File srcDir, File dir, List<String> packages, Map<String, List<String>> contents,
			String extension) {
		String packageName = parseJavaName(srcDir, dir);
		packages.add(packageName);
		File[] ls = dir.listFiles();
		List<String> dirContents = new ArrayList<>();
		contents.put(packageName, dirContents);
		for (File f : ls) {
			if (f.isDirectory()) {
				scan(srcDir, f, packages, contents, extension);
			} else if (f.getName().endsWith(extension) || "*".equals(extension)) {
				dirContents.add(parseJavaName(srcDir, f));
			}
		}
	}

	public static List<File> discoverSiblingProjects() {
		return discoverSiblingProjects(new File("").getAbsoluteFile(), "src/main/java");
	}

	public static List<File> discoverSiblingProjects(File relativeStart, String sourcesDirectoryPath) {
		List<File> siblingProjects = new ArrayList<>();
		File rootProject = relativeStart.getParentFile();
		System.err.println("Root Project is: " + rootProject.getName());
		System.err.println("Current Project is: " + relativeStart.getName());
		File[] siblingDirs = rootProject.listFiles(f -> f.isDirectory());
		for (File sibling : siblingDirs) {
			File javaSource = new File(sibling, sourcesDirectoryPath);
			if (javaSource.exists()) {
				siblingProjects.add(sibling);
			}
		}
		return siblingProjects;
	}

	public static String parseJavaName(File srcDir, File javaItem) {
		if (srcDir.equals(javaItem)) {
			return "";
		}
		String srcPath = srcDir.getPath();
		String path = javaItem.getPath();
		if (!path.startsWith(srcPath)) {
			throw new IllegalArgumentException("javaItem must be a sub directory or file of srcDir");
		}
		int relativeIndex = srcPath.length() + 1;
		String result = path.substring(relativeIndex);
		switch (File.separatorChar) {
		case '\\':
			result = result.replaceAll("[\\\\]", ".");
			break;
		default:
			result = result.replaceAll("[/]", ".");
		}
		if (javaItem.isFile()) {
			result = result.replaceAll("([.]class|[.]java)$", "");
		}
		return result;
	}

	public static final File getJavaFile(File srcDir, String javaName) {
		return getFile(srcDir, javaName, ".java");
	}

	public static final File getClassFile(File srcDir, String javaName) {
		return getFile(srcDir, javaName, ".class");
	}

	public static final File getPackageFile(File srcDir, String javaName) {
		return getFile(srcDir, javaName, "");
	}

	private static File getFile(File srcDir, String javaName, String ext) {
		return new File(srcDir, javaName.replaceAll("[.]", File.separator).concat(ext));
	}

	public static String[] getClassPathElements() {
		String classpath = System.getProperty("java.class.path");
		String[] classPathValues = classpath.split(File.pathSeparator);
		return classPathValues;
	}

	public static void printClassPathElements() {
		System.out.println("Class Path:");
		System.out.println();
		for (String classPath : getClassPathElements()) {
			System.out.println(classPath);
		}
		System.out.println();
	}

	public static File[] getClassPathJars() {
		List<File> files = new ArrayList<>();
		for (String classPath : getClassPathElements()) {
			if (classPath.endsWith(".jar")) {
				files.add(new File(classPath));
			}
		}
		return files.toArray(new File[0]);
	}

	private final File sourceDirectory;
	private final String basePackage;
	private final List<String> packages;
	private final Map<String, List<String>> contents;

	private ProjectSource(File sourceDirectory, String basePackage, List<String> packages,
			Map<String, List<String>> contents) {
		this.sourceDirectory = sourceDirectory;
		this.basePackage = basePackage;
		this.packages = packages;
		this.contents = contents;
	}

	public File getSourceDirectory() {
		return sourceDirectory;
	}

	public String getBasePackage() {
		return basePackage;
	}

	public List<String> getPackages() {
		return packages;
	}

	public List<String> getContents(String packageName) {
		if (contents.containsKey(packageName)) {
			List<String> result = new ArrayList<>();
			result.addAll(contents.get(packageName));
			return result;
		}
		return Collections.emptyList();
	}
}
