package NBD;

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
                    "1 - Wyswietlic informacje\n" +
                    "2 - Wypozyczyc pojazd\n" +
                    "3 - Zwrocic pojazd\n" +
                    "Aby wyjsc kliknij wprowadz dowolna inna wartosc.\n ");
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
                    rentalApi.wypozycz(vehicle, client, days);
                    break;
                case 3:

                    break;
                default:
                    exit(0);
            }
        } while (true);


    }
}