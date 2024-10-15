import NBD.Car;
import NBD.DatabaseApi;
import NBD.Vehicle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseApiTest {
    @Test
    public void insertToDatabaseTest() {
        DatabaseApi api = new DatabaseApi();
        Vehicle car = new Car("test", 1900, 340, 4);
        api.addEntity(car);
        Vehicle car2 = api.getEntity(Vehicle.class, car.getId());
        assertEquals(car.getName(), car2.getName());
        assertEquals(car.getPower(), car2.getPower());
    }

    @Test
    public void updateVehicleInDatabaseTest() {
        DatabaseApi api = new DatabaseApi();
        Vehicle car = new Car("update_test",2900,640,4);
        api.addEntity(car);
        Vehicle car2 = api.getEntity(Vehicle.class, car.getId());
        assertEquals(car.getName(), car2.getName());
        car.setName("name_changed");
        api.updateEntity(car);

        assertEquals(api.getEntity(Vehicle.class, car.getId()).getName(), "name_changed");
    }

    @Test
    public void deleteVehicleInDatabaseTest() {
        DatabaseApi api = new DatabaseApi();
        Vehicle car = new Car("delete_test",2900,640,4);
        api.addEntity(car);
        api.deleteEntity(Vehicle.class, car.getId());
        Vehicle car2 = api.getEntity(Vehicle.class, car.getId());
        assertNull(car2);
    }
}
