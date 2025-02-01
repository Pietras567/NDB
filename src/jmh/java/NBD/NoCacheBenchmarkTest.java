package NBD;

import org.bson.types.ObjectId;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark test class for measuring the performance of fetching an entity
 * from a cache when there is no existing cache entry.
 * This class uses JMH (Java Microbenchmark Harness) annotations to define
 * the benchmarking parameters and processes.
 * The benchmark simulates retrieval of an entity without caching, to measure the
 * average time taken for such operation.
 */
@State(Scope.Benchmark)
public class NoCacheBenchmarkTest {
    private CacheManager cacheManager;
    private Car car;
    private Class<Car> carClass;
    private ObjectId carId;
    private RedisManager redisManager;

    @Setup(Level.Trial)
    public void setUp() {
        cacheManager = new CacheManager();
        redisManager = new RedisManager();
        car = new Car("Honda Civic", 1400, 120, 4);
        cacheManager.addEntity(car, "vehicles");
        carClass = Car.class;
        carId = car.getId();
    }

    @Setup(Level.Invocation)
    public void clearCache() {
        redisManager.removeDocument("vehicles:"+car.getId().toString().replaceFirst("^0+(?!$)", ""));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 5, warmups = 1)
    @Warmup(iterations = 5, time = 2)
    @Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
    public void getEntityWithNoCache() {
        cacheManager.getEntity(carClass, "vehicles", carId);
    }

}
