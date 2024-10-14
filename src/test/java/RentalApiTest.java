import NBD.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class RentalApiTest {
    @Test
    public void getAllEntitiesTest() {
        RentalApi rentalApi = new RentalApi();
        DatabaseApi databaseApi = new DatabaseApi();
        Client client = new Client("Tom", 24);
        Vehicle car = new Car("Yaris", 1300, 200, 4);

        databaseApi.addEntity(client);
        databaseApi.addEntity(car);

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        Rent rent = new Rent(client.getId(), car.getId(),start,end);

        databaseApi.addEntity(rent);
        Rent temp = (Rent) rentalApi.getAllEntities(Rent.class);
    }

    @Test
    public void oddajTest() {

    }

    @Test
    public void wypozyczTest() {

    }
}
