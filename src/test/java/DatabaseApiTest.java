import Producer.Car;
import Producer.DatabaseApi;
import Producer.Vehicle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseApiTest {

    @Test
    public void insertToDatabaseTest() {
        DatabaseApi api = new DatabaseApi();
        Vehicle car = new Car("test", 1900, 340, 4);
        api.addEntity(car, "vehicles");
        Vehicle car2 = api.getEntity(Vehicle.class, "vehicles", car.getId());
        assertEquals(car.getName(), car2.getName());
        assertEquals(car.getPower(), car2.getPower());
    }

    @Test
    public void updateVehicleInDatabaseTest() {
        DatabaseApi api = new DatabaseApi();
        Vehicle car = new Car("update_test",2900,640,4);
        api.addEntity(car, "vehicles");
        Vehicle car2 = api.getEntity(Vehicle.class, "vehicles", car.getId());
        assertEquals(car.getName(), car2.getName());
        car.setName("name_changed");
        api.updateEntity(car, "vehicles", car.getId());

        assertEquals(api.getEntity(Vehicle.class, "vehicles", car.getId()).getName(), "name_changed");
    }

    @Test
    public void deleteVehicleInDatabaseTest() {
        DatabaseApi api = new DatabaseApi();
        Vehicle car = new Car("delete_test",2900,640,4);
        api.addEntity(car, "vehicles");
        api.deleteEntity(Vehicle.class,"vehicles" ,car.getId());
        Vehicle car2 = api.getEntity(Vehicle.class,"vehicles" ,car.getId());
        assertNull(car2);
    }
}
