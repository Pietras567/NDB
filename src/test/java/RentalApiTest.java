import NBD.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains unit tests for the RentalApi class.
 * It verifies the proper functioning of key RentalApi methods, such as
 * rent and returnVehicle, through both positive and negative
 * test cases. Each test interacts with mocked database operations provided
 * by the DatabaseApi class to simulate real-world scenarios in a controlled
 * environment. The cleanup method ensures isolation of test cases by
 * clearing out testing data after each test run.
 */
public class RentalApiTest {

    private final DatabaseApi databaseApi = new DatabaseApi();

    public void cleanup() {
        DatabaseApi api = new DatabaseApi();
        Iterable<Client> clients = api.getAllEntities(Client.class, "clients");
        for (Client client : clients) {
            api.deleteEntity(Client.class, "clients", client.getId());
        }

        Iterable<Vehicle> vehicles = api.getAllEntities(Vehicle.class, "vehicles");
        for (Vehicle vehicle : vehicles) {
            api.deleteEntity(Vehicle.class, "vehicles", vehicle.getId());
        }

        Iterable<Rent> rents = api.getAllEntities(Rent.class, "test_rents");
        for (Rent rent : rents) {
            api.deleteEntity(Rent.class, "rents", rent.getId());
        }
    }


    @Test
    public void returnVehiclePositiveTest() {
        RentalApi rentalApi = new RentalApi();
        Client client = new Client("Pepe", 54);
        Vehicle car = new Car("i20", 1300, 100, 5);
        databaseApi.addEntity(client, "clients");
        databaseApi.addEntity(car,"vehicles");
        assertTrue(rentalApi.rent(car, client, 50));
        assertTrue(rentalApi.returnVehicle(car, client));
        this.cleanup();
    }

    @Test
    public void returnVehicleNegativeTest() {
        RentalApi rentalApi = new RentalApi();
        Client client = new Client("antonio", 44);
        Vehicle car = new Car("i30", 1300, 200, 5);
        databaseApi.addEntity(client, "clients");
        databaseApi.addEntity(car, "vehicles");
        assertTrue(rentalApi.rent(car, client, 0));
        assertFalse(rentalApi.returnVehicle(car, client));
        this.cleanup();
    }

    @Test
    public void rentPositiveTest() {
        RentalApi rentalApi = new RentalApi();
        Vehicle motorbike = new Motorbike("kawasaki", 350,250,1);
        databaseApi.addEntity(motorbike, "vehicles");
        Vehicle car = databaseApi.getEntity(Vehicle.class, "vehicles" ,motorbike.getId());
        Client client = new Client("Adrian", 24);
        databaseApi.addEntity(client, "clients");

        assertTrue(rentalApi.rent(car, client, 2));

        Iterable<Rent> temp = databaseApi.getAllEntities(Rent.class, "test_rents");
        Rent last = temp.iterator().next();

        assertEquals(last.getClient_id(), client.getId());
        this.cleanup();
    }

    @Test
    public void rentNegativeTest() {
        RentalApi rentalApi = new RentalApi();
        Vehicle rented = new Truck("scania", 4000, 460, 14000);
        Client client = new Client("Bob", 24);
        databaseApi.addEntity(rented, "vehicles");
        databaseApi.addEntity(client, "clients");
        assertTrue(rentalApi.rent(rented, client, 100));
        assertFalse(rentalApi.rent(rented, client, 1));
        this.cleanup();
    }
}
