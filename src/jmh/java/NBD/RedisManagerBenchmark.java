package NBD;

import org.bson.types.ObjectId;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark class for measuring the performance of RedisManager operations with caching enabled.
 * Provides methods to set up the environment and run benchmarks targeting specific functionalities.
 *
 * This class utilizes JMH (Java Microbenchmark Harness) for performance assessment by focusing
 * on average execution time over a series of iterations. The setup phase initializes dependencies
 * such as RedisManager, an in-memory CacheManager, and a test entity (Car).
 *
 * Annotations are used to configure JMH parameters, including warmup, measurement, and the number
 * of forks. The primary benchmark method evaluates a Redis retrieval operation.
 */
@State(Scope.Benchmark)
public class RedisManagerBenchmark {
    private RedisManager redisManager;
    private CacheManager cacheManager;
    private Car car;
    private Class<Car> carClass;
    private ObjectId carId;

    @Setup(Level.Trial)
    public void setUp() {
        redisManager = new RedisManager();
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
        redisManager.getDocument("vehicles:"+carId.toString().replaceFirst("^0+(?!$)", ""));
    }
}
