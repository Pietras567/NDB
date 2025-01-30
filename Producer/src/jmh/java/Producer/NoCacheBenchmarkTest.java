package Producer;

import org.bson.types.ObjectId;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

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
