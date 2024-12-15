package NBD;

import java.util.Scanner;
import java.util.UUID;

import static java.lang.System.exit;

public class Main {
    public static void main(String[] args) {
        DatabaseApi databaseApi = new DatabaseApi();


        RentalApi rentalApi = new RentalApi();
        Vehicle car1 = new Car("yaris", 1500, 261, 5);
        Vehicle car2 = new Car("126p", 700, 30, 5);
        Vehicle car3 = new Car("poldon", 1200, 161, 5);

        databaseApi.addEntity(car1, "vehicles");
        databaseApi.addEntity(car2, "vehicles");
        databaseApi.addEntity(car3, "vehicles");


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
                            for (Vehicle v : databaseApi.getAllEntities(Vehicle.class, "vehicles")) {
                                System.out.println(v.toString());
                            }
                            break;

                        case 2:
                            for (Client c : databaseApi.getAllEntities(Client.class, "clients")) {
                                System.out.println(c.toString());
                            }
                            break;

                        case 3:
                            for (Rent r : databaseApi.getAllEntities(Rent.class, "rents")) {
                                System.out.println(r);
                            }
                            break;
                    }
                    break;
                case 2:
                    System.out.println("Podaj id pojazdu ktory chcesz wypozyczyc : \n");
                    UUID vehicleId = UUID.fromString(scanner.next());
                    System.out.println("Podaj id swojego profilu : \n");
                    UUID clientId = UUID.fromString(scanner.next());
                    System.out.println("Podaj na jak dlugo wypozyczasz (dni) : \n");
                    int days = scanner.nextInt();

                    Client client = databaseApi.getEntity(Client.class, "clients", clientId);
                    Vehicle vehicle = databaseApi.getEntity(Vehicle.class, "vehicles", vehicleId);
                    rentalApi.wypozycz(vehicle, client, days);
                    break;
                case 3:
                    System.out.println("Podaj id pojazdu do zwrotu : \n");
                    UUID returnedVehicleId = UUID.fromString(scanner.next());
                    System.out.println("Podaj id swojego profilu : \n");
                    UUID returningClientId = UUID.fromString(scanner.next());
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