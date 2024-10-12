package NBD;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("Witamy w CarRental!");

        DatabaseApi Api = new DatabaseApi();
        RentalApi rApi = new RentalApi();

        // Dodawanie pojazdu
        Car car = new Car("Ferrari", 1700, 1200, 2);
        Api.addVehicle(car);

        // Pobieranie pojazdu
        Vehicle retrievedVehicle = Api.getVehicle(car.getId());
        System.out.println("Retrieved Vehicle: " + retrievedVehicle.getName());
        System.out.println("Retrieved Vehicle power: " + retrievedVehicle.getPower());
        System.out.println("Retrieved Vehicle id: " + retrievedVehicle.getId());

        // Aktualizacja pojazdu
        retrievedVehicle.setName("Tuned Ferrari");
        Api.updateVehicle(retrievedVehicle);

        System.out.println("");

        // Pobieranie pojazdu zaktualizowanego
        Vehicle retrievedVehicle2 = Api.getVehicle(car.getId());
        System.out.println("Updated Retrieved Vehicle: " + retrievedVehicle2.getName());
        System.out.println("Updated Retrieved Vehicle power: " + retrievedVehicle2.getPower());
        System.out.println("Updated Retrieved Vehicle id: " + retrievedVehicle2.getId());

        System.out.println("");

        Client client = new Client("Adam", 22);

        Api.addClient(client);

        rApi.wypozycz(car, client, 22);
        System.out.println(LocalDateTime.now());

        //rApi.wypozycz(Api.getVehicle(1), client);

        rApi.oddaj(Api.getVehicle(16), Api.getClient(16));
    }
}