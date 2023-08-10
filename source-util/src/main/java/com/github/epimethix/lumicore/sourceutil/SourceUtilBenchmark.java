package com.github.epimethix.lumicore.sourceutil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import com.github.epimethix.lumicore.benchmark.Benchmark;
import com.github.epimethix.lumicore.benchmark.Benchmark.Check;

public class SourceUtilBenchmark {
//	SourceUtilBenchmark:Scan All Projects took 187,418,615,193 nanos :: 2023-08-10
	public static void main(String[] args) throws IOException {
//		try {
//			Check readSQLRepository = Benchmark.start(SourceUtilBenchmark.class, "read source");
//			JavaSource js = JavaSource.readFile(new File(""));
//			readSQLRepository.stop();
//			Benchmark.printBenchmarkResults();
//			try (PrintStream ps = new PrintStream(new File(""))) {
//				js.printDebug(ps);
//			} catch (Exception e2) {}
//		} catch (Exception e2) {
//			e2.printStackTrace();
//		}
//		if (System.currentTimeMillis() > 0) {
//			return;
//		}
		List<File> siblingProjects = ProjectSource.discoverSiblingProjects();
		Map<String, JavaSource> sources = new HashMap<>();
		Check checkScanAll = Benchmark.start(SourceUtilBenchmark.class, "Scan All Projects");
		for (File projectDir : siblingProjects) {
			Check checkScanProject = Benchmark.start(SourceUtilBenchmark.class, "scan project " + projectDir.getName());
			/*
			 * sibling projects are identified by the "src/main/java" directory by default
			 */
			System.err.println("## Scanning " + projectDir.getPath());
			/*
			 * scan for .java files
			 */
			try (Stream<Path> s = Files.find(projectDir.toPath().resolve("src/main/java"), Integer.MAX_VALUE,
					(path, attrs) -> path.getFileName().toString().toLowerCase(Locale.ENGLISH).endsWith(".java"));) {
				s.forEach(javaFile -> {
					Check checkReadSource = Benchmark.start(SourceUtilBenchmark.class,
							"read " + javaFile.getFileName());
					try {
						JavaSource src = JavaSource.readSource(Files.readString(javaFile));
						String className = src.getClassName();
						sources.put(className, src);
					} catch (Exception e) {
						System.err.println(javaFile.toString() + " could not be read!");
						e.printStackTrace();
					}
					checkReadSource.stop();
					long t = checkReadSource.time();
					if (t > 1_000_000_000)
						System.err.printf("%nreading '%s' took %,d nanos%n%n", javaFile.toString(),
								checkReadSource.time());
				});
			}
			checkScanProject.stop();
		}
		checkScanAll.stop();
		Benchmark.printBenchmarkResults();
	}
}
