package NBD;

import java.time.Instant;
import java.util.UUID;


/**
 * The RentalApi class provides functionalities for renting and returning vehicles.
 * It interacts with `DatabaseApi` to perform operations on rental records.
 */
public class RentalApi {
    private DatabaseApi databaseApi = new DatabaseApi();

    /**
     * Processes the return of a rented vehicle. Verifies if the vehicle is currently rented,
     * checks the association with the provided client, and updates the rent details if applicable.
     *
     * @param vehicle The vehicle to be returned.
     * @param client The client attempting to return the vehicle.
     * @return true if the return process succeeds and the rental is ended, otherwise false.
     */
    public boolean returnVehicle(Vehicle vehicle, Client client) {
        try {
            UUID vehicleId = vehicle.getId();

            Iterable<Rent> list = databaseApi.getRents(vehicleId);
            boolean wypozyczony = false;
            Rent rent = null;
            if(list.iterator().hasNext()) {
                for (Rent r : list) {
                    if((Instant.now().isAfter(r.getStartDate()) || Instant.now().equals(r.getStartDate())) && (Instant.now().isBefore(r.getEndDate())) || Instant.now().equals(r.getEndDate())) {
                        wypozyczony = true;
                        rent = r;
                    }
                }
            }

            if(wypozyczony) {
                if(rent.getClient_id().equals(client.getId())) {
                    rent.setEndDate(Instant.now());
                    databaseApi.updateEntity(rent, "rents");
                    System.out.println("Pojazd zostal zwrocony.");
                } else {
                    System.out.println("Pojazd byl wypozyczony przez innego klienta. Nie mozesz go zwrocic.");
                    return false;
                }
            } else {
                System.out.println("Nie mozna zwrocic pojazdu, nie jest on aktualnie wypozyczony.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Napotkano problem podczas proby wczesniejszego zakanczania wypozyczenia.");
            return false;
        }
        return true;
    }


    /**
     * Attempts to rent a vehicle for a specified number of days to a given client.
     * Checks if the vehicle is available for the provided duration and proceeds
     * with the rental if it is. Updates the database with the rental information.
     *
     * @param vehicle The vehicle requested for rental.
     * @param client The client attempting to rent the vehicle.
     * @param days The number of days the vehicle is to be rented.
     * @return true if the rental is successful, false if the rental fails due to
     *         the vehicle being unavailable or an error occurring during processing.
     */
    public boolean rent(Vehicle vehicle, Client client, int days) {
        try {
            UUID vehicleId = vehicle.getId();

            Iterable<Rent> list = databaseApi.getRents(vehicleId);

            boolean wypozyczony = false;
            if(list.iterator().hasNext()) {
                for (Rent r : list) {
                    if((Instant.now().isAfter(r.getStartDate()) || Instant.now().equals(r.getStartDate())) && (Instant.now().isBefore(r.getEndDate())) || Instant.now().equals(r.getEndDate())) {
                        wypozyczony = true;
                    }
                }
            }

            if(!wypozyczony) {
                Rent rent = new Rent(client.getId(), vehicle.getId(), Instant.now(), Instant.now().plusSeconds((long) days *24*60*60));
                databaseApi.addEntity(rent, "rents");
                System.out.println("Pojazd zostal wypozyczony.");
            } else {
                System.out.println("Nie mozna wypozyczyc pojazdu, jest on aktualnie wypozyczony.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Napotkano problem podczas proby wypozyczenia pojazdu.");
            return false;
        }
        return true;
    }
}
