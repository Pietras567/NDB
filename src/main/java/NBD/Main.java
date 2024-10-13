package NBD;

import java.time.LocalDateTime;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DatabaseApi Api = new DatabaseApi();
        RentalApi rApi = new RentalApi();

        /* SEKCJA TESTOWA
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

        rApi.oddaj(Api.getVehicle(19), Api.getClient(19));
        */


        System.out.println("\nWitamy w CarRental!\n");
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.print("Podaj co zrobic:\n" +
                    "1 - Wyswietlic informacje\n" +
                    "2 - Wypozyczyc pojazd\n" +
                    "3 - Zwrocic pojazd\n" +
                    "Aby wyjsc kliknij wprowadz dowolna inna wartosc. ");
            int choice1 = scanner.nextInt();
            switch (choice1) {
                case 1:
                    System.out.print("Podaj co wyswietlic (1 - Pojazdy, 2 - klientow, 3 - Wypozyczenia: ");
                    int choice2 = scanner.nextInt();
                    switch (choice2) {
                        case 1:
                            for (Vehicle v : rApi.getAllVehicles()) {
                                System.out.println(v);
                            }
                            break;

                        case 2:
                            for (Client c : rApi.getAllClients()) {
                                System.out.println(c);
                            }
                            break;

                        case 3:
                            for (Rent r : rApi.getAllRents()) {
                                System.out.println(r);
                            }
                            break;
                    }
                    break;
                case 2:

                    break;
                case 3:
                    break;
                default:
                    break;
            }
        } while (true);


    }
}