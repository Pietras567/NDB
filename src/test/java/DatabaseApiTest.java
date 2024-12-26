import NBD.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseApiTest {

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

        Iterable<Rent> rents = api.getAllEntities(Rent.class, "rents");
        for (Rent rent : rents) {
            api.deleteEntity(Rent.class, "rents", rent.getId());
        }
    }

    @Test
    public void insertToDatabaseTest() {
        DatabaseApi api = new DatabaseApi();
        Vehicle car = new Car("test", 1900, 340, 4);
        api.addEntity(car, "vehicles");
        Vehicle car2 = api.getEntity(Vehicle.class, "vehicles", car.getId());
        assertEquals(car.getName(), car2.getName());
        assertEquals(car.getPower(), car2.getPower());
        this.cleanup();
    }

    @Test
    public void updateVehicleInDatabaseTest() {
        DatabaseApi api = new DatabaseApi();
        Vehicle car = new Car("update_test",2900,640,4);
        api.addEntity(car, "vehicles");
        Vehicle car2 = api.getEntity(Vehicle.class, "vehicles", car.getId());
        assertEquals(car.getName(), car2.getName());
        car.setName("name_changed");
        api.updateEntity(car, "vehicles");

        assertEquals("name_changed", api.getEntity(Vehicle.class, "vehicles", car.getId()).getName());
        this.cleanup();
    }

    @Test
    public void deleteVehicleInDatabaseTest() {
        DatabaseApi api = new DatabaseApi();
        Vehicle car = new Car("delete_test",2900,640,4);
        api.addEntity(car, "vehicles");
        api.deleteEntity(Vehicle.class, "vehicles", car.getId());
        Vehicle car2 = api.getEntity(Vehicle.class, "vehicles", car.getId());
        assertNull(car2);
        this.cleanup();
    }

    @Test
    public void getAllClientsTest() {
        DatabaseApi api = new DatabaseApi();

        // clear clients table
        Iterable<Client> it = api.getAllEntities(Client.class, "clients");

        for (Client client : it) {
            api.deleteEntity(Client.class, "clients", client.getId());
        }

        Client newClient1 = new Client("Adrian", 40);
        Client newClient2 = new Client("Tomek", 20);
        // add multiple clients
        api.addEntity(newClient1, "clients");
        api.addEntity(newClient2, "clients");

        Iterable<Client> newIt = api.getAllEntities(Client.class, "clients");

        List<Client> clients = new ArrayList<>();
        for (Client client : newIt) {
            clients.add(client);
        }

        int counter = 0;
        for (Client client : clients) {
            if (client.getId().equals(newClient1.getId()) || client.getId().equals(newClient2.getId())) {
                counter++;
            }
        }

        assertEquals(2, clients.size());
        assertEquals(2, counter);

        this.cleanup();
    }

    @Test
    public void getRentsByVehicleTest() throws InterruptedException {
        DatabaseApi api = new DatabaseApi();
        RentalApi rentalApi = new RentalApi();


        Car car = new Car("car_test",2900,640,4);
        Client client1 = new Client("Adrian", 30);
        Client client2 = new Client("Tom", 40);

        api.addEntity(car, "vehicles");
        api.addEntity(client1, "clients");
        api.addEntity(client2, "clients");

        Iterable<Rent> rents = api.getRents(car.getId());
        for (Rent rent : rents) {
            api.deleteEntity(Rent.class, "rents", rent.getId());
        }

        rentalApi.wypozycz(car, client1, 0);
        Thread.sleep(2000);

        rentalApi.wypozycz(car, client2, 0);

        rents = api.getRents(car.getId());
        List<Rent> rentList = new ArrayList<>();

        for (Rent rent : rents) {
            rentList.add(rent);
        }

        assertEquals(2, rentList.size());
//        System.out.println("TUTAJ");
//        rents.forEach(System.out::println);
//        System.out.println(client1.getId());
//        System.out.println(client2.getId());
//        System.out.println(rentList.get(0).getClient_id());
//        System.out.println(rentList.get(1).getClient_id());
//        System.out.println(rentList);
//        System.out.println("TUTAJ");

        int counter = 0;
        for (Rent rent : rentList) {
            if (rent.getClient_id().equals(client1.getId()) || rent.getClient_id().equals(client2.getId())) {
                counter++;
            }
        }
        assertEquals(2, counter);
        assertEquals(car.getId(), rentList.get(0).getVehicle_id());
        assertEquals(car.getId(), rentList.get(1).getVehicle_id());

        this.cleanup();
    }
}
