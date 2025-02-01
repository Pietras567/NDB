import Producer.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The RentalApiTest class is a test suite for validating the functionality of the RentalApi class.
 * It contains several unit tests to ensure the correct behavior of methods related to the rental system,
 * such as retrieving all entities, returning vehicles, and renting vehicles.
 *
 * This test class relies on an auxiliary DatabaseApi for handling storage and management of
 * test data entities, including Clients, Vehicles, and Rents.
 *
 * Test cases included:
 * - getAllEntitiesTest: Validates retrieval of all Rent entities and ensures the correct properties of the last rent record.
 * - returnVehiclePositiveTest: Verifies the behavior of returning a rented vehicle under positive conditions.
 * - returnVehicleNegativeTest: Checks the behavior when attempting to return a vehicle that cannot be returned.
 * - rentPositiveTest: Confirms successful rental of a vehicle by a client under valid conditions.
 * - rentNegativeTest: Ensures that a vehicle cannot be rented again if it is already rented.
 *
 * Each test uses assertions to verify expected outcomes, ensuring correctness of the RentalApi's behavior.
 */
public class RentalApiTest {

    private final DatabaseApi databaseApi = new DatabaseApi();
    @Test
    public void getAllEntitiesTest() {
        RentalApi rentalApi = new RentalApi();
        Client client = new Client("Tom", 24);
        Vehicle car = new Car("Yaris", 1300, 200, 4);

        databaseApi.addEntity(client, "clients");
        databaseApi.addEntity(car, "vehicles");

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        Rent rent = new Rent(client.getId(), car.getId(),start,end);

        databaseApi.addEntity(rent, "rents");
        List<Rent> temp = rentalApi.getAllEntities(Rent.class);
        Rent last = temp.getLast();
        assertEquals(rent.getId(), last.getId());
        assertEquals(rent.getClient().getId(), last.getClient().getId());
    }

    @Test
    public void returnVehiclePositiveTest() {
        RentalApi rentalApi = new RentalApi();
        Client client = new Client("Pepe", 54);
        Vehicle car = new Car("i20", 1300, 100, 5);
        databaseApi.addEntity(client, "clients");
        databaseApi.addEntity(car, "vehicles");
        assertTrue(rentalApi.rent(car, client, 50));
        assertTrue(rentalApi.returnVehicle(car, client));
        List<Rent> temp = rentalApi.getAllEntities(Rent.class);
        Rent last = temp.getLast();
        assertEquals(last.getEndDate().getDayOfMonth(), LocalDateTime.now().getDayOfMonth());
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

        List<Rent> temp = rentalApi.getAllEntities(Rent.class);
        Rent last = temp.getLast();

        assertEquals(last.getClient().getId(), client.getId());
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
    }
}
