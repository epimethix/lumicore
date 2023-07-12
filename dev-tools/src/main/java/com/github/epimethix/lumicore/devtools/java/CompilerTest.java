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
package com.github.epimethix.lumicore.devtools.java;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

//import org.apache.commons.io.FileUtils;

public class CompilerTest {
	public static void test(File dir) throws InstantiationException, IllegalAccessException, ClassNotFoundException,
			IOException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (!dir.exists()) {
			dir.mkdir();
		}

		String className = "mypackage.MyClass";
		{
			// @formatter:off
			String javaCode = 
					"package mypackage;\n" + 
					"public class MyClass implements Runnable {\n" + 
					"    public void run() {\n" + 
					"        System.out.println(\"Hello World\");\n" + 
					"    }\n" + 
					"}\n";
			// @formatter:on
			File testFile = new File(dir, className.replaceAll("[.]", "/") + ".java");
//			FileUtils.writeStringToFile(testFile, javaCode, Charset.defaultCharset(), false);
		}
		{
			long nanoTime = System.nanoTime();
			compile(dir, System.err);
			System.out.printf("ClassGenerator.compile took %,d nanos%n", System.nanoTime() - nanoTime);
		}
		{
			URL url = dir.toURI().toURL();
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { url });
			Class<?> cls = Class.forName(className, true, classLoader);

			Runnable runner = (Runnable) cls.getConstructor().newInstance();
			Thread t = new Thread(runner);
			t.start();
		}
	}

	private static List<File> collectJavaFiles(File dir, List<File> result) {
		return collectFiles(dir, result, (f) -> f.isFile() && f.getName().endsWith(".java"));
	}
	
	private static List<File> collectFiles(File dir, List<File> result, FileFilter filter) {
		File[] fileArray = dir.listFiles(filter);
		if (Objects.nonNull(fileArray)) {
			result.addAll(Arrays.asList(fileArray));
		}
		File[] subDirs = dir.listFiles((f) -> f.isDirectory());
		if (Objects.nonNull(subDirs)) {
			for (File subDir : subDirs) {
				collectFiles(subDir, result, filter);
			}
		}
		return result;
	}

	public static void compile(File dir, PrintStream diagnosticPrintStream) throws IOException {
		List<File> files = collectJavaFiles(dir, new ArrayList<>());
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
			Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
			compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits).call();
			for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
				diagnosticPrintStream.format("Error on line %d in %s%n", diagnostic.getLineNumber(),
						diagnostic.getSource().toString());
			}
		}
	}
}
