import NBD.Car;
import NBD.DatabaseApi;
import NBD.Vehicle;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for verifying the functionality of the DatabaseApi class.
 * This class contains unit tests to ensure that the core operations of
 * the DatabaseApi, such as insertion, updating, and deletion of entities,
 * are working as expected.
 *
 * The test cases in this class validate:
 * - Adding entities to the database.
 * - Retrieving entities from the database.
 * - Updating existing entities in the database.
 * - Deleting entities from the database.
 *
 * The main entity used in the tests is Vehicle.
 */
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
