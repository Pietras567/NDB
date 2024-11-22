import NBD.*;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CacheTest {
    CacheManager cacheManager = new CacheManager();
    @BeforeEach
    void setUp() {

    }

    @Test
    void insertReadCacheManagerTest() {
        Car car = new Car("Honda Civic", 1400, 120, 4);
        cacheManager.addEntity(car, "vehicles");
        Car loadedCar = cacheManager.getEntity(Car.class, "vehicles", car.getId());

        RedisManager redisManager = new RedisManager();
        System.out.println(redisManager.getDocument("vehicles:"+car.getId().toString().replaceFirst("^0+(?!$)", "")));
        ///todo dodać kolejny test, a w nim asercje na zapisywanie i pobieranie tylko z cache, żeby można było sprawdzić działanie czysto cache'a

        assertEquals(car.getClass(), loadedCar.getClass());
        assertNotNull(loadedCar);

        assertEquals(car.getId(), loadedCar.getId());
        assertEquals(car.getName(), loadedCar.getName());
        assertEquals(car.getSeats(), loadedCar.getSeats());
        assertEquals(car.getPower(), loadedCar.getPower());
        assertEquals(car.getWeight(), loadedCar.getWeight());

        //System.out.println(loadedCar.getSeats());
        //System.out.println(loadedCar.toString());
    }
}
