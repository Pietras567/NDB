import NBD.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

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
    public void oddajPositiveTest() {
        RentalApi rentalApi = new RentalApi();
        Client client = new Client("Pepe", 54);
        Vehicle car = new Car("i20", 1300, 100, 5);
        databaseApi.addEntity(client, "clients");
        databaseApi.addEntity(car,"vehicles");
        assertTrue(rentalApi.wypozycz(car, client, 50));
        assertTrue(rentalApi.oddaj(car, client));
        this.cleanup();
    }

    @Test
    public void oddajNegativeTest() {
        RentalApi rentalApi = new RentalApi();
        Client client = new Client("antonio", 44);
        Vehicle car = new Car("i30", 1300, 200, 5);
        databaseApi.addEntity(client, "clients");
        databaseApi.addEntity(car, "vehicles");
        assertTrue(rentalApi.wypozycz(car, client, 0));
        assertFalse(rentalApi.oddaj(car, client));
        this.cleanup();
    }

    @Test
    public void wypozyczPositiveTest() {
        RentalApi rentalApi = new RentalApi();
        Vehicle motorbike = new Motorbike("kawasaki", 350,250,1);
        databaseApi.addEntity(motorbike, "vehicles");
        Vehicle car = databaseApi.getEntity(Vehicle.class, "vehicles" ,motorbike.getId());
        Client client = new Client("Adrian", 24);
        databaseApi.addEntity(client, "clients");

        assertTrue(rentalApi.wypozycz(car, client, 2));

        Iterable<Rent> temp = databaseApi.getAllEntities(Rent.class, "test_rents");
        Rent last = temp.iterator().next();

        assertEquals(last.getClient_id(), client.getId());
        this.cleanup();
    }

    @Test
    public void wypozyczNegativeTest() {
        RentalApi rentalApi = new RentalApi();
        Vehicle rented = new Truck("scania", 4000, 460, 14000);
        Client client = new Client("Bob", 24);
        databaseApi.addEntity(rented, "vehicles");
        databaseApi.addEntity(client, "clients");
        assertTrue(rentalApi.wypozycz(rented, client, 100));
        assertFalse(rentalApi.wypozycz(rented, client, 1));
        this.cleanup();
    }
}
