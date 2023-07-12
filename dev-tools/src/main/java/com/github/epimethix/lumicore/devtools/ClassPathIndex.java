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
package com.github.epimethix.lumicore.devtools;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.util.ClassPath;

import com.github.epimethix.lumicore.benchmark.Benchmark;
import com.github.epimethix.lumicore.benchmark.Benchmark.Check;
import com.github.epimethix.lumicore.sourceutil.ProjectSource;

//import com.sun.org.apache.bcel.internal.util.ClassPath;
// org.eclipse.justj.openjdk.hotspot.jre.full.linux.x86_64_15.0.1.v20201027-0507/jre/lib/modules
public final class ClassPathIndex {
//	private static int N = 0;

//	private final static class SpiderThread implements Runnable {
//		@Override
//		public void run() {
//
//		}
//	}

//	private static int n = 0;
//	private static int n_inner = 0;

	private static class JrtFileVisitor implements FileVisitor<Path> {

		private final Pattern anonymousClassPattern = Pattern.compile("\\$\\d+\\.class$");
		private final Pattern classPattern = Pattern.compile("\\.class$");

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//			if(dir.getNameCount() <= 1) {
//				System.out.println(dir.toString());
//			} else {
//				return FileVisitResult.SKIP_SUBTREE;
//			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			String fileName = file.getFileName().toString();
			if (fileName.equals("module-info.class")) {
				return FileVisitResult.CONTINUE;
			} else if (anonymousClassPattern.matcher(fileName).find()) {
				return FileVisitResult.CONTINUE;
			} else if (file.getNameCount() > 2 && classPattern.matcher(fileName).find()) {
				String className = file.subpath(2, file.getNameCount()).toString().replaceAll("([/]|[\\\\])", ".")
						.replaceAll("\\.class$", "");
				String simpleName = className.substring(className.lastIndexOf(".") + 1);
				className = className.replaceAll("\\$", ".");
				simpleName = simpleName.replaceAll("\\$", ".");
				addClass(simpleName, className, String.format("jrt:/modules/%s", file.getName(1).toString()), false,
						true);
//				System.out.println(className);
//				if (simpleName.contains(".")) {
//					n_inner++;
//				} else {
//					n++;
//				}
			}
			return FileVisitResult.CONTINUE;
//			if (classNamePath.getNameCount() > 1 && (classNamePath.getName(0).toString().equals("java")
//					&& classNamePath.getName(1).toString().equals("util"))) {
//
//			} else if (classNamePath.getNameCount() == 1) {
//				System.err.println(classNamePath);
//				System.out.print(classNamePath);
//				System.out.print(" ... " + classNamePath.getName(0));
//				if (classNamePath.getNameCount() > 1)
//					System.out.print(" ... " + classNamePath.getName(1));
//				System.out.println();
//				System.out.print(classNamePath.getName(0));
//			}
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}

	}

	private final static Thread indexSystemLibrary() {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				Check ck = Benchmark.start(ClassPathIndex.class, "index system library");
				FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
				Path start = fs.getPath("modules");
				JrtFileVisitor fsw = new JrtFileVisitor();

				try {
					Files.walkFileTree(start, fsw);
					System.err.println("FINISHED");
				} catch (IOException e) {
					e.printStackTrace();
				}
//				List<String> result = resolveNames("String");
				System.out.println(resolveIndex("String"));
				System.out.println(resolveIndex("String").size());
				ck.stop();
			}
		};
		Thread t = new Thread(r);
		t.start();
		return t;
	}

//
	private final static class RunIndexing implements Runnable {
		private final File inputFile;
		private final boolean projects;
		private final boolean src;

		private RunIndexing(File inputFile, boolean src) {
			this(inputFile, false, src);
		}

		private RunIndexing(File inputFile, boolean projects, boolean src) {
			this.inputFile = inputFile;
			this.projects = projects;
			this.src = src;
		}

		@Override
		public void run() {
			if (src) {
				scanSrc(inputFile, projects);
			} else {
				scanBin(inputFile, projects);
			}
		}

//		private final void scanSrc(File inputFile) {
//			scanSrc(inputFile, false);
//		}

		private final void scanSrc(File inputFile, boolean projects) {
			scan(inputFile, projects, "java");
		}

//		private final void scanBin(File inputFile) {
//			scanBin(inputFile, false);
//		}

		private final void scanBin(File inputFile, boolean projects) {
			scan(inputFile, projects, "class");
		}

		private final void scan(File inputFile, boolean projects, String extension) {
			if (inputFile.isFile() && (inputFile.getName().endsWith(".jar") || inputFile.getName().endsWith(".zip"))) {
				scanJar(inputFile, projects, extension);
			} else if (inputFile.isDirectory()) {
				scanDir(inputFile, extension);
			}
		}

		private final void scanDir(File inputDir, String extension) {
			Check ckScan = Benchmark.start(ClassPathIndex.class, "scanDir(" + inputDir.getName() + ")",
					"ClassPathIndex.scan");
			extension = "." + extension;
			boolean readOnly = ".class".equals(extension);
			scanDir(inputDir.getPath(), inputDir, extension, inputDir.getPath().length() + 1, readOnly);
			ckScan.stop();
		}

		private final void scanDir(String parentDir, File inputDir, String extension, int relativeIndex,
				boolean readOnly) {
//			final String ext = extension;
			File[] classes = inputDir.listFiles((f) -> f.isFile() && f.getName().endsWith(extension));
			if (Objects.nonNull(classes) && classes.length > 0) {
				for (File cls : classes) {
					String className = cls.getPath().substring(relativeIndex).replaceAll("([/]|[\\\\])", ".")
							.replaceAll("\\" + extension + "$", "");
					if (className.contains(".")) {
						String simpleName = className.substring(className.lastIndexOf(".") + 1).replaceAll("\\$", ".");
						className = className.replaceAll("\\$", ".");
//						addClass(simpleName, className);
						addClass(simpleName, className, parentDir, extension.equals(".java"), readOnly);
					} else {
						className = className.replaceAll("\\$", ".");
//						addClass(className, className);
						addClass(className, className, parentDir, extension.equals(".java"), readOnly);
					}
				}
			}
			File[] dirs = inputDir.listFiles((f) -> f.isDirectory());
			if (Objects.nonNull(dirs) && dirs.length > 0) {
				for (File dir : dirs) {
					scanDir(parentDir, dir, extension, relativeIndex, readOnly);
				}
			}
		}

		private final void scanJar(File inputFile, boolean projects, String extension) {
			Check ckScan = Benchmark.start(ClassPathIndex.class, "scanJar(" + inputFile.getName() + ")",
					"ClassPathIndex.scan");
			File[] filesToSearch;
//			File inputFile = new File(jarFile.getPath());
			if (inputFile.isDirectory()) {
				filesToSearch = inputFile.listFiles((f) -> f.isFile() && f.getName().endsWith(".jar"));
			} else {
				filesToSearch = new File[] { inputFile };
			}
			Pattern validClassNamePattern = Pattern.compile(".+\\." + extension + "$");
//			List<Class<?>> classes = new ArrayList<>();
			String fileLocation = inputFile.getPath();
			String location = fileLocation;
			for (File file : filesToSearch) {
				try (JarFile jarFile = new JarFile(file)) {
					Enumeration<JarEntry> e = jarFile.entries();
					while (e.hasMoreElements()) {
						JarEntry jarEntry = e.nextElement();
						String jarEntryName = jarEntry.getName();
						if (validClassNamePattern.matcher(jarEntryName).matches()) {
							String subDir = projects ? jarEntryName.substring(0, jarEntryName.indexOf("/")) : null;
							if (Objects.nonNull(subDir) && !location.endsWith(subDir)) {
								location = fileLocation + "!/" + subDir;
							}
							String className = projects ? jarEntryName.substring(jarEntryName.indexOf("/") + 1)
									: jarEntryName;
							className = className.replaceAll("/", ".").replaceAll("\\." + extension + "$", "");
							if (className.contains(".")) {
								String simpleName;
								simpleName = className.substring(className.lastIndexOf(".") + 1).replaceAll("\\$", ".");
								className = className.replaceAll("\\$", ".");
//								addClass(simpleName, className);
								addClass(simpleName, className, location, "java".equals(extension), true);
							} else {
								className = className.replaceAll("\\$", ".");
								addClass(className, className, location, "java".equals(extension), true);
							}
						}
					}
				} catch (IOException e) {
//					LOGGER.error(e);
//					e.printStackTrace();
				}
			}
//			classCache.put(packageName, classes);
			ckScan.stop();
		}
	}

	public final static class IndexedClass implements Comparable<IndexedClass> {
		private final String className;
		private final URI location;
		private final boolean source;
		private final boolean readOnly;

		private IndexedClass(String className, URI location, boolean source, boolean readOnly) {
			this.className = className;
			this.location = location;
			this.source = source;
			this.readOnly = readOnly;
		}

		public String getClassName() {
			return className;
		}

		public URI getLocation() {
			return location;
		}

		public boolean isSource() {
			return source;
		}

		public boolean isReadOnly() {
			return readOnly;
		}

		@Override
		public int compareTo(IndexedClass o) {
			return className.compareTo(o.className);
		}

		@Override
		public String toString() {
			return className;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((className == null) ? 0 : className.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IndexedClass other = (IndexedClass) obj;
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			return true;
		}
	}

	private final static Map<String, Set<IndexedClass>> classMap = new HashMap<>();

	private static List<String> simpleNames;

	private static boolean indexed;

	private ClassPathIndex() {}

	public static List<String> getSimpleNames() {
		if (Objects.isNull(simpleNames)) {
			simpleNames = new ArrayList<>(classMap.keySet()); // :351
			Collections.sort(simpleNames);
		}
		return simpleNames;
		/* @formatter:off
		Caused by: java.lang.ArrayIndexOutOfBoundsException: Index 7011 out of bounds for length 7011
			at java.base/java.util.HashMap.keysToArray(HashMap.java:950)
			at java.base/java.util.HashMap$KeySet.toArray(HashMap.java:993)
			at java.base/java.util.ArrayList.<init>(ArrayList.java:181)
			at com.github.epimethix.lumicore.devtools.ClassPathIndex.getSimpleNames(ClassPathIndex.java:351)
			at com.github.epimethix.lumicore.devtools.gui.diagram.dialog.AddEntityDialog.<init>(AddEntityDialog.java:57)
		
		Caused by: java.lang.ArrayIndexOutOfBoundsException: Index 5904 out of bounds for length 5904
			at java.base/java.util.HashMap.keysToArray(HashMap.java:950)
			at java.base/java.util.HashMap$KeySet.toArray(HashMap.java:993)
			at java.base/java.util.ArrayList.<init>(ArrayList.java:181)
			at com.github.epimethix.lumicore.devtools.ClassPathIndex.getSimpleNames(ClassPathIndex.java:351)
			at com.github.epimethix.lumicore.devtools.gui.diagram.dialog.AddEntityDialog.<init>(AddEntityDialog.java:57)
		
		Caused by: java.lang.ArrayIndexOutOfBoundsException: Index 8308 out of bounds for length 8308
		 @formatter:on */
	}

	
	private synchronized final static void addClass(String simpleName, String fullName, String basePath, boolean source,
			boolean readOnly) {
		putClass(simpleName, fullName, basePath, source, readOnly);
		while (simpleName.contains(".")) {
			simpleName = simpleName.substring(simpleName.indexOf(".") + 1);
			putClass(simpleName, fullName, basePath, source, readOnly);
		}
	}

	private final static void putClass(String simpleName, String fullName, String basePath, boolean source,
			boolean readOnly) {
		Set<IndexedClass> knownClasses = classMap.get(simpleName);
		if (Objects.isNull(knownClasses)) {
			knownClasses = new HashSet<>();
			classMap.put(simpleName, knownClasses);
		}
		URI location;
		if (basePath.contains(".jar!") || basePath.endsWith(".jar")) {
			try {
				location = new URI("jar", basePath, null);
				knownClasses.add(new IndexedClass(fullName, location, source, readOnly));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				location = new URI("file", basePath, null);
				knownClasses.add(new IndexedClass(fullName, location, source, readOnly));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//			N++;
	}

	public final static void startIndexing(boolean wait) {
		Check ckStartIndexing = Benchmark.start(ClassPathIndex.class, "startIndexing(wait = " + wait + ")");
		String[] cp = ClassPath.getClassPath().split(File.pathSeparator);
//		String[] cp = .getClassPath().split(File.pathSeparator);
		Pattern projectBinDir = Pattern.compile("([^/]+)[/]bin[/]([^/]+)$");
		Pattern projectBinDirW = Pattern.compile("([^\\\\]+)[\\\\]bin[\\\\]([^\\\\]+)$");
		List<Thread> threads = new ArrayList<>();
		for (String c : cp) {
			Matcher m = projectBinDir.matcher(c);
			File projectDir = null;
			String sourceSet = null;
			if (m.find()) {
				sourceSet = m.group(2);
				projectDir = new File(c).getParentFile().getParentFile();
			} else {
				Matcher mw = projectBinDirW.matcher(c);
				if (mw.find()) {
					sourceSet = m.group(2);
					projectDir = new File(c).getParentFile().getParentFile();
				}
			}
			if (Objects.nonNull(projectDir)) {
				File srcDir = Path.of(projectDir.getPath(), "src", sourceSet, "java").toFile();
				if (srcDir.exists()) {
					threads.add(scanSrc(srcDir));
				}
			}
		}
//		threads.add(scanSrc(DevToolsFiles.SRC_ZIP, true));
		threads.add(indexSystemLibrary());
		File[] jars = ProjectSource.getClassPathJars();
		for (File jar : jars) {
			threads.add(scanBin(jar));
		}
		if (wait) {
			for (Thread t : threads) {
				while (true) {
					try {
						t.join();
						break;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		indexed = true;
		ckStartIndexing.stop();
	}

	private static Thread scanBin(File inputFile) {
		return scanBin(inputFile, false);
	}

	private static Thread scanBin(File inputFile, boolean projects) {
		RunIndexing ri = new RunIndexing(inputFile, projects, true);
		Thread t = new Thread(ri);
		t.start();
		return t;
	}

	private static Thread scanSrc(File inputFile) {
		return scanSrc(inputFile, false);
	}

	private static Thread scanSrc(File inputFile, boolean projects) {
		RunIndexing ri = new RunIndexing(inputFile, projects, true);
		Thread t = new Thread(ri);
		t.start();
		return t;
	}

	public final static Optional<IndexedClass> getIndex(String fullName) {
		String simpleName;
		if (fullName.contains(".")) {
			simpleName = fullName.substring(fullName.lastIndexOf(".") + 1);
		} else {
			simpleName = fullName;
		}
		Set<IndexedClass> indexedClasses = classMap.get(simpleName);
		if (Objects.nonNull(indexedClasses)) {
			for (IndexedClass ic : indexedClasses) {
				if (fullName.equals(ic.className)) {
					return Optional.of(ic);
				}
			}
		}
		return Optional.empty();
	}

	public final static List<IndexedClass> resolveIndex(String simpleName) {
		if (!indexed) {
			startIndexing(true);
		}
		Set<IndexedClass> result = classMap.get(simpleName);
		if (Objects.nonNull(result)) {
			List<IndexedClass> list = new ArrayList<>(result);
			Collections.sort(list);
			return list;
		} else {
			return Collections.emptyList();
		}
	}

	public final static List<String> resolveNames(String simpleName) {
		if (!indexed) {
			startIndexing(true);
		}
		Set<IndexedClass> result = classMap.get(simpleName);
		if (Objects.nonNull(result)) {
			List<String> list = new ArrayList<>();
			for (IndexedClass ic : result) {
				list.add(ic.className);
			}
			Collections.sort(list);
			return list;
		} else {
			return Collections.emptyList();
		}
	}
}
