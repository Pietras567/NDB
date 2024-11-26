import NBD.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

public class CacheTest {
    CacheManager cacheManager = new CacheManager();
    DatabaseApi databaseApi = new DatabaseApi();
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
        redisManager.removeDocument("vehicles:"+car.getId().toString().replaceFirst("^0+(?!$)", ""));

        Car loadedCar2 = cacheManager.getEntity(Car.class, "vehicles", car.getId());
        System.out.println(redisManager.getDocument("vehicles:"+car.getId().toString().replaceFirst("^0+(?!$)", "")));

        assertEquals(car.getClass(), loadedCar.getClass());
        assertEquals(car.getClass(), loadedCar2.getClass());
        assertNotNull(loadedCar);
        assertNotNull(loadedCar2);

        assertEquals(car.getId(), loadedCar.getId());
        assertEquals(car.getName(), loadedCar.getName());
        assertEquals(car.getSeats(), loadedCar.getSeats());
        assertEquals(car.getPower(), loadedCar.getPower());
        assertEquals(car.getWeight(), loadedCar.getWeight());

        assertEquals(car.getId(), loadedCar2.getId());
        assertEquals(car.getName(), loadedCar2.getName());
        assertEquals(car.getSeats(), loadedCar2.getSeats());
        assertEquals(car.getPower(), loadedCar2.getPower());
        assertEquals(car.getWeight(), loadedCar2.getWeight());
    }
    @Test
    void updateReadCacheManagerTest() {
        Car car = new Car("Honda Civic", 1400, 120, 4);
        cacheManager.addEntity(car, "vehicles");
        Car updateCar = cacheManager.getEntity(Car.class, "vehicles", car.getId());
        updateCar.setName("Honda inna");
        updateCar.setSeats(2);
        cacheManager.updateEntity(updateCar, "vehicles", car.getId());

        Car resultCar = cacheManager.getEntity(Car.class, "vehicles", car.getId());

        assertEquals("Honda inna", resultCar.getName());
        assertNotEquals(car.getName(), resultCar.getName());
        assertNotEquals(car.getSeats(), resultCar.getSeats());
        assertEquals(car.getId(), resultCar.getId());
    }

    @Test
    void deleteReadCacheManagerTest() {
        Car car = new Car("Honda Civic", 1400, 120, 4);
        cacheManager.addEntity(car, "vehicles");
        cacheManager.deleteEntity(Car.class, "vehicles", car.getId());
        Car deleteCar = cacheManager.getEntity(Car.class, "vehicles", car.getId());

        assertNull(deleteCar);

    }

    @Test
    void ttlReadCacheManagerTest() {
        Car car = new Car("Honda Civic", 1400, 120, 4);
    }

}
