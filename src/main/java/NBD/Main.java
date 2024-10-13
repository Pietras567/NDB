package NBD;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        System.out.println("Witamy w CarRental!");

        DatabaseApi Api = new DatabaseApi();
        Vehicle car = Api.getVehicle(3);
        car.setName("Fabia gt");
        Api.updateVehicle(car);

        // Dodawanie pojazdu
//        Car car = new Car("gtr", 2700, 820, 2);
//        Api.addVehicle(car);
//
//        // Pobieranie pojazdu
//        Vehicle retrievedVehicle = Api.getVehicle(car.getId());
//        System.out.println("Retrieved Vehicle: " + retrievedVehicle.getName());
//        System.out.println("Retrieved Vehicle power: " + retrievedVehicle.getPower());
//        System.out.println("Retrieved Vehicle id: " + retrievedVehicle.getId());


//        // Aktualizacja pojazdu
//        retrievedVehicle.setName("Tuned Ferrari");
//        Api.updateVehicle(retrievedVehicle);
//
//        System.out.println("");
//
//        // Pobieranie pojazdu zaktualizowanego
//        Vehicle retrievedVehicle2 = Api.getVehicle(car.getId());
//        System.out.println("Updated Retrieved Vehicle: " + retrievedVehicle2.getName());
//        System.out.println("Updated Retrieved Vehicle power: " + retrievedVehicle2.getPower());
//        System.out.println("Updated Retrieved Vehicle id: " + retrievedVehicle2.getId());
//
//        System.out.println("");
//
//        Client client = new Client("Adam", 22);
//
//        Api.addClient(client);
//
//        Rent rent = new Rent(client.getId(), car.getId(), LocalDate.now(), LocalDate.now().plusWeeks(2));
//        rent.setClient(Api.getClient(client.getId()));
//        rent.setVehicle(Api.getVehicle(client.getId()));
//        Api.addRent(rent);
    }
}