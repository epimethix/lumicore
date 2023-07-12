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
package com.github.epimethix.lumicore.benchmark;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * This class is used to create a collection of time measurements mainly during
 * startup of the application. That way a time profile for an application in an
 * environment can be analyzed.
 * 
 * @author epimethix
 *
 */
public class Benchmark {
	private final static Map<String, Check> checkMap = new HashMap<>();
	private final static List<String> keyList = new ArrayList<>();
	private final static List<String> sequenceKeyList = new ArrayList<>();

	private final static long measureTimeAvg;

	private static int startLevel;

	static {
		long t = 0L;
		for (int i = 0; i < 10; i++) {

			Check noop = start(Benchmark.class, "noop" + (i + 1), "noop");
			noop.stop();
			if (i > 0) {
				t += noop.time();
			}
		}
		Check noop = start(Benchmark.class, "noop");
		noop.stop();
		t += noop.time();
		measureTimeAvg = t / 10L;
	}

	/**
	 * The class {@code Check} contains a single time measurement.
	 * 
	 * @author epimethix
	 *
	 */
	public final static class Check {
		private final Class<?> cls;
		private final String operation;
		private final String sequence;
		private final String unique;
		private final long start;
		private final int startLevel;
		private long stop;

		private Check(Class<?> cls, String operation, String sequence, int startLevel) {
			this.cls = cls;
			this.operation = operation;
			this.sequence = sequence;
			this.startLevel = startLevel;
			start = System.nanoTime();
			unique = UUID.randomUUID().toString();
		}

		/**
		 * Sets the ending time of this measurement.
		 */
		public synchronized final void stop() {
			stop = System.nanoTime();
			keyList.add(toString());
			if (Objects.isNull(sequence)) {
				Benchmark.startLevel--;
			}
		}

		/**
		 * Creates the difference of stop minus start and returns it. if the
		 * {@code Check} was not stopped the negative start time will be returned.
		 * 
		 * @return the measured time in nanoseconds
		 */
		public long time() {
			return stop - start;
		}

		@Override
		public String toString() {
			return String.format("%s.%s.%s", cls.getName(), operation, Integer.toHexString(hashCode()));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((unique == null) ? 0 : unique.hashCode());
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
			Check other = (Check) obj;
			if (unique == null) {
				if (other.unique != null)
					return false;
			} else if (!unique.equals(other.unique))
				return false;
			return true;
		}
	}

	/**
	 * Starts a single operation time measurement.
	 * 
	 * @param cls       the class in which the measurement is taken
	 * @param operation the name of the operation that is measured
	 * @return a new {@code Check} that is initialized with the current nano time.
	 */
	public synchronized final static Check start(Class<?> cls, String operation) {
		return start(cls, operation, null);
	}

	/**
	 * Starts a recurring time measurement that is grouped by a sequence name.
	 * 
	 * @param cls       the class in which the measurement is taken
	 * @param operation the name of the operation that is measured
	 * @param sequence  the sequence name to group measurements by
	 * @return a new {@code Check} that is initialized with the current nano time.
	 */
	public synchronized final static Check start(Class<?> cls, String operation, String sequence) {
//		String key = String.format("%s.%s", cls.getName(), operation);
		Check ck;
		if (Objects.nonNull(sequence)) {
			ck = new Check(cls, operation, sequence, 0);
		} else {
			ck = new Check(cls, operation, sequence, startLevel++);
		}
		checkMap.put(ck.toString(), ck);
		if (Objects.nonNull(sequence) && !sequenceKeyList.contains(sequence)) {
			sequenceKeyList.add(sequence);
		}
		return ck;
	}

	/**
	 * Prints the benchmark results to {@code System.out}.
	 */
	public final static void printBenchmarkResults() {
		printBenchmarkResults(System.out);
	}

	/**
	 * Prints the benchmark results to the supplied {@code PrintStream}.
	 * 
	 * @param stream a {@code PrintStream} to print the benchmark results to
	 */
	public static void printBenchmarkResults(PrintStream stream) {
		stream.printf("%nBENCHMARK RESULTS%n");
		stream.printf(
				"%nestimated measuring time was ~%,d nanos by average%n"
						+ "%d measurements took ~%,d nanos in total%n%n",
				measureTimeAvg, checkMap.size(), measureTimeAvg * checkMap.size());
		List<String> keyListReversed = new ArrayList<>(keyList);
		Collections.reverse(keyListReversed);
		for (String key : keyListReversed) {
			Check ck = checkMap.get(key);
			if (Objects.nonNull(ck)) {
				if (ck.stop > 0) {
					if (Objects.isNull(ck.sequence)) {
						printResult(stream, ck);
					}
				}
			}
		}
		if (sequenceKeyList.size() > 0) {
			stream.printf("%n### Sequence Totals:%n%n");
			for (String sequence : sequenceKeyList) {
				Check min = null;
				Check max = null;
				int count = 0;
				long seqTotal = 0L;
				for (String key : keyList) {
					Check ck = checkMap.get(key);
					if (Objects.nonNull(ck)) {
						if (Objects.nonNull(ck.sequence) && ck.sequence.equals(sequence)) {
							if (ck.stop > 0) {
								long seq = ck.stop - ck.start;
								if (Objects.isNull(min) || seq < min.time()) {
									min = ck;
								}
								if (Objects.isNull(max) || seq > max.time()) {
									max = ck;
								}
								seqTotal += seq;
								count++;
							}
						}
					}
				}
				if (seqTotal > 0L) {
					stream.printf("Sequence %s took %,d nanos in total%n", sequence, seqTotal);
					stream.printf("%d samples took by average %,d nanos%n", count, seqTotal / count);
					stream.print("Min: ");
					printResult(stream, min);
					stream.print("Max: ");
					printResult(stream, max);
					stream.println();
				}
			}
		}
		Set<String> keySet = checkMap.keySet();
		for (String key : keySet) {
			Check ck = checkMap.get(key);
			if (ck.stop == 0) {
				stream.printf("Check %s.%s was never stopped!%n%n", ck.cls.getSimpleName(), ck.operation);
			}
		}
	}

	private static void printResult(PrintStream stream, Check ck) {
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < ck.startLevel; i++) {
//			sb.append("  ");
//		}
		stream.printf("%s%s:%s took %,d nanos%n", "| ".repeat(ck.startLevel), ck.cls.getSimpleName(), ck.operation,
				ck.time());
	}
}
