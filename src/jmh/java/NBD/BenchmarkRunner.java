package NBD;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * The BenchmarkRunner class is responsible for executing JMH benchmark tests.
 *
 * It includes and runs benchmarks for the following test classes:
 * - CacheBenchmarkTest: Benchmarks operations with caching enabled.
 * - NoCacheBenchmarkTest: Benchmarks operations with caching disabled.
 * - DatabaseApiBenchmark: Benchmarks operations using a database API.
 * - RedisManagerBenchmark: Benchmarks operations using Redis as a storage backend.
 *
 * This class uses the JMH OptionsBuilder to configure and execute each benchmark test.
 * Each test configuration includes the benchmark class name, and the tests are run
 * sequentially as part of the main method execution.
 *
 * The main method is designed to be executed manually, such as via a build tool or
 * directly from the console to measure performance metrics across the included test scenarios.
 */
public class BenchmarkRunner {
    public static void main(String[] args) throws Exception { //Z konsoli ./gradlew jmh
        Options options = new OptionsBuilder()
                .include(CacheBenchmarkTest.class.getSimpleName())
                .build();

        new Runner(options).run();

        Options options2 = new OptionsBuilder()
                .include(NoCacheBenchmarkTest.class.getSimpleName())
                .build();

        new Runner(options2).run();

        Options options3 = new OptionsBuilder()
                .include(DatabaseApiBenchmark.class.getSimpleName())
                .build();

        new Runner(options3).run();

        Options options4 = new OptionsBuilder()
                .include(RedisManagerBenchmark.class.getSimpleName())
                .build();

        new Runner(options4).run();
    }
}

