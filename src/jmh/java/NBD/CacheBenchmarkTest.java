package NBD;

import NBD.CacheManager;
import NBD.Car;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark test for measuring the performance of cache operations
 * using the CacheManager.
 *
 * This class evaluates the performance of retrieving entities
 * from a cached collection, specifically analyzing average retrieval time.
 *
 * The test setup includes initializing a CacheManager instance,
 * creating a test entity (Car), and adding the entity to a cache.
 *
 * Annotations:
 * - @State(Scope.Benchmark): Indicates that the state of this class will be
 *   shared across benchmark threads.
 * - @Benchmark: Marks a method to be benchmarked.
 * - @BenchmarkMode: Specifies the mode of benchmarking (e.g., AverageTime).
 * - @OutputTimeUnit: Sets the output time unit for benchmark results.
 * - @Fork, @Warmup, and @Measurement: Configuration annotations to control
 *   the benchmarking process including iterations, warm-up, forks, and time units.
 *
 * Benchmark Method:
 * - {@code getEntityWithCache()}: Benchmarks the CacheManager's ability to
 *   retrieve entities from a cache using specific parameters.
 *
 * Dependencies:
 * - Requires the JMH (Java Microbenchmark Harness) library for benchmarking.
 * - Assumes the presence of `CacheManager`, `Car`, and `ObjectId` types.
 */
@State(Scope.Benchmark)
public class CacheBenchmarkTest {
    private CacheManager cacheManager;
    private Car car;
    private Class<Car> carClass;
    private ObjectId carId;

    @Setup(Level.Trial)
    public void setUp() {
        cacheManager = new CacheManager();
        car = new Car("Honda Civic", 1400, 120, 4);
        cacheManager.addEntity(car, "vehicles");
        carClass = Car.class;
        carId = car.getId();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 5, warmups = 1)
    @Warmup(iterations = 5, time = 2)
    @Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
    public void getEntityWithCache() {
        cacheManager.getEntity(carClass, "vehicles", carId);
    }

}
