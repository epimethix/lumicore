# lumicore :: Benchmark

The benchmark project is used to monitor the time it takes to start the application.

there can be single tasks that are measured or there can be series of measurements.

## Usage

```java
		Check check = Benchmark.start(Test.class, "Test Operation");
		// Do work
		check.stop();
		
		for(int i = 0; i < 10; i++) {
			check = Benchmark.start(Test.class, "Step " + i, "Test Series");
			// work
			check.stop();
		}
		
		System.err.printf("Test Operation took %,d nanos", check.time());
		
		Benchmark.printBenchmarkResults(System.out);
```
