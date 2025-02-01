package NBD;

import java.util.Scanner;

import static java.lang.System.exit;

/**
 * The Main class serves as the entry point for the CarRental application. It provides a
 * console-based interface for users to interact with the application and perform various
 * actions such as viewing information, renting vehicles, returning vehicles, and registering
 * as a client.
 *
 * Users can navigate through different functionalities by selecting options in a menu-driven
 * format. The application communicates with a rental API and a database API to retrieve and
 * manipulate data related to vehicles, clients, and rentals.
 *
 * Functionalities:
 * 1. Display Information: Users can view details about vehicles, clients, and rentals.
 * 2. Rent a Vehicle: Users can rent a specific vehicle by providing its ID and their client ID.
 * 3. Return a Vehicle: Users can return a rented vehicle by providing its ID and their client ID.
 * 4. Register as a Client: Users can register as a new client by providing their name and age.
 *
 * The application continues in a loop until the user chooses to exit by providing an invalid
 * input for the main menu.
 */
public class Main {
    public static void main(String[] args) {
        DatabaseApi databaseApi = new DatabaseApi();
        RentalApi rentalApi = new RentalApi();

        System.out.println("\nWitamy w CarRental!\n");
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.print("Podaj co zrobic:\n" +
                    "1 - Wyswietlic informacje\n" +
                    "2 - Wypozyczyc pojazd\n" +
                    "3 - Zwrocic pojazd\n" +
                    "4 - Zarejestruj sie\n" +
                    "Aby wyjsc wprowadz dowolna inna wartosc.\n ");
            int choice1 = scanner.nextInt();
            switch (choice1) {
                case 1:
                    System.out.print("Podaj co wyswietlic (1 - Pojazdy, 2 - klientow, 3 - Wypozyczenia) : \n");
                    int choice2 = scanner.nextInt();
                    switch (choice2) {
                        case 1:
                            for (Vehicle v : rentalApi.getAllEntities(Vehicle.class)) {
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
                    int vehicleId = scanner.nextInt();
                    System.out.println("Podaj id swojego profilu : \n");
                    int clientId = scanner.nextInt();
                    System.out.println("Podaj na jak dlugo wypozyczasz (dni) : \n");
                    int days = scanner.nextInt();

                    Client client = databaseApi.getEntity(Client.class, clientId);
                    Vehicle vehicle = databaseApi.getEntity(Vehicle.class, vehicleId);
                    rentalApi.rent(vehicle, client, days);
                    break;
                case 3:
                    System.out.println("Podaj id pojazdu do zwrotu : \n");
                    int returnedVehicleId = scanner.nextInt();
                    System.out.println("Podaj id swojego profilu : \n");
                    int returningClientId = scanner.nextInt();
                    Vehicle returnedVehicle = databaseApi.getEntity(Vehicle.class, returnedVehicleId);
                    Client returningClient = databaseApi.getEntity(Client.class, returningClientId);
                    rentalApi.returnVehicle(returnedVehicle, returningClient);
                    break;
                case 4:
                    System.out.println("Podaj swoje imie : \n");
                    String clientName = scanner.next();
                    System.out.println("Podaj swoj wiek : \n");
                    int age = scanner.nextInt();
                    databaseApi.addEntity(new Client(clientName, age));
                    break;
                default:
                    exit(0);
            }
        } while (true);
    }
}