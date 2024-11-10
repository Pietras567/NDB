package NBD;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Scanner;

import static java.lang.System.exit;

public class Main {
    public static void main(String[] args) {
        DatabaseApi databaseApi = new DatabaseApi();
        RentalApi rentalApi = new RentalApi();


        System.out.println("\nWitamy w CarRental!\n");
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.print("Podaj co zrobic:\n" +
                    "1 - Wyswietl informacje\n" +
                    "2 - Wypozycz pojazd\n" +
                    "3 - Zwroc pojazd\n" +
                    "4 - Zarejestruj sie\n" +
                    "Aby wyjsc wprowadz dowolna inna wartosc.\n ");
            int choice1 = scanner.nextInt();
            switch (choice1) {
                case 1:
                    System.out.print("Podaj co wyswietlic (1 - Pojazdy, 2 - klientow, 3 - Wypozyczenia) : \n");
                    int choice2 = scanner.nextInt();
                    switch (choice2) {
                        case 1:
                            for (Vehicle v : rentalApi.getAllVehicles(Vehicle.class)) {
                                System.out.println(v.toString());
                            }
                            break;

                        case 2:
                            for (Client c : rentalApi.getAllEntities(Client.class)) {
                                System.out.println(c.toString());
                            }
                            break;

                        case 3:
                            for (Rent r : rentalApi.getAllEntities(Rent.class)) {
                                System.out.println(r);
                            }
                            break;
                    }
                    break;
                case 2:
                    System.out.println("Podaj id pojazdu ktory chcesz wypozyczyc : \n");
                    ObjectId vehicleId = new ObjectId((scanner.next()));
                    System.out.println("Podaj id swojego profilu : \n");
                    ObjectId clientId = new ObjectId((scanner.next()));
                    System.out.println("Podaj na jak dlugo wypozyczasz (dni) : \n");
                    int days = scanner.nextInt();

                    Client client = databaseApi.getEntity(Client.class, "clients", clientId);
                    Vehicle vehicle = databaseApi.getEntity(Vehicle.class, "vehicles", vehicleId);
                    rentalApi.wypozycz(vehicle, client, days);
                    break;
                case 3:
                    System.out.println("Podaj id pojazdu do zwrotu : \n");
                    ObjectId returnedVehicleId = new ObjectId(String.valueOf(scanner.nextInt()));
                    System.out.println("Podaj id swojego profilu : \n");
                    ObjectId returningClientId = new ObjectId(String.valueOf(scanner.nextInt()));
                    Vehicle returnedVehicle = databaseApi.getEntity(Vehicle.class, "vehicles", returnedVehicleId);
                    Client returningClient = databaseApi.getEntity(Client.class, "clients", returningClientId);
                    rentalApi.oddaj(returnedVehicle, returningClient);
                    break;
                case 4:
                    System.out.println("Podaj swoje imie : \n");
                    String clientName = scanner.next();
                    System.out.println("Podaj swoj wiek : \n");
                    int age = scanner.nextInt();
                    databaseApi.addEntity(new Client(clientName, age), "clients");
                    break;
                default:
                    exit(0);
            }
        } while (true);
    }
}