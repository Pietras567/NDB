package Producer;

import org.bson.types.ObjectId;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * This class is a benchmark test suite for the DatabaseApi class and its related operations,
 * leveraging JMH (Java Microbenchmark Harness) to measure the performance of methods
 * in controlled scenarios.
 *
 * The benchmark evaluates the performance of retrieving an entity from the database
 * using the `DatabaseApi.getEntity` method.
 *
 * An instance of the `CacheManager` is also prepared during the setup phase to store
 * initial entities for benchmarking.
 *
 * A test entity of type `Car` is created and added to the database during the setup phase.
 * The car's metadata, such as its class and ID, is used as parameters in benchmarking scenarios.
 *
 * Annotations used in the class:
 * - {@code @State(Scope.Benchmark)} defines the scope of instances created for benchmarking (single instance per benchmark thread).
 * - {@code @Setup(Level.Trial)} initializes the required objects and environment before the benchmark trials are executed.
 * - {@code @Benchmark} marks the method to be executed as part of the benchmark.
 * - {@code @BenchmarkMode(Mode.AverageTime)} measures the average time taken to execute the benchmarked method.
 * - {@code @OutputTimeUnit(TimeUnit.MILLISECONDS)} specifies that the output time for benchmarks is reported in milliseconds.
 * - {@code @Fork(value = 5, warmups = 1)} runs five separate iterations of the benchmark, with one warm-up iteration.
 * - {@code @Warmup(iterations = 5, time = 2)} defines warm-up iterations (5 iterations, each lasting 2 seconds).
 * - {@code @Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)} defines actual measurement iterations (5 iterations, each lasting 2 seconds).
 */
@State(Scope.Benchmark)
public class DatabaseApiBenchmark {
    private DatabaseApi databaseApi;
    private CacheManager cacheManager;
    private Car car;
    private Class<Car> carClass;
    private ObjectId carId;

    @Setup(Level.Trial)
    public void setUp() {
        databaseApi = new DatabaseApi();
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
    public void getEntityWithDatabaseApi() {
        databaseApi.getEntity(carClass, "vehicles", carId);
    }
}
