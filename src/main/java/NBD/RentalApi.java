package NBD;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The RentalApi class provides functionalities for renting and returning vehicles.
 * It interacts with `DatabaseApi` to perform operations on rental records.
 */
public class RentalApi {
    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
    DatabaseApi databaseApi = new DatabaseApi();

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
            long vehicleId = vehicle.getId();
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            String jpql = "SELECT r FROM Rent r WHERE r.vehicle_id = :vehicleId";
            TypedQuery<Rent> query = entityManager.createQuery(jpql, Rent.class);
            query.setParameter("vehicleId", vehicleId);

            List<Rent> list = query.getResultList();
            boolean wypozyczony = false;
            Rent rent = null;
            if(!list.isEmpty()) {
                for (Rent r : list) {
                    if((LocalDateTime.now().isAfter(r.getStartDate()) || LocalDateTime.now().isEqual(r.getStartDate())) && (LocalDateTime.now().isBefore(r.getEndDate())) || LocalDateTime.now().isEqual(r.getEndDate())) {
                        wypozyczony = true;
                        rent = r;
                    }
                }
            }

            if(wypozyczony) {
                if(rent.getClient().getId() == client.getId()) {
                    rent.setEndDate(LocalDateTime.now());
                    databaseApi.updateEntity(rent);
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
            System.out.println("Napotkano problem podczas proby wczesniejszego zakonczenia wypozyczenia.");
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
            long vehicleId = vehicle.getId();
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            String jpql = "SELECT r FROM Rent r WHERE r.vehicle_id = :vehicleId";
            TypedQuery<Rent> query = entityManager.createQuery(jpql, Rent.class);
            query.setParameter("vehicleId", vehicleId);

            List<Rent> list = query.getResultList();
            boolean wypozyczony = false;
            if(!list.isEmpty()) {
                for (Rent r : list) {
                    if((LocalDateTime.now().isAfter(r.getStartDate()) || LocalDateTime.now().isEqual(r.getStartDate())) && (LocalDateTime.now().isBefore(r.getEndDate())) || LocalDateTime.now().isEqual(r.getEndDate())) {
                        wypozyczony = true;
                    }
                }
            }

            if(!wypozyczony) {
                Rent rent = new Rent(client.getId(), vehicle.getId(), LocalDateTime.now(), LocalDateTime.now().plusDays(days));
                rent.setClient(client);
                rent.setVehicle(vehicle);
                databaseApi.addEntity(rent);
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

    /**
     * Retrieves all entities of a specified type from the database.
     *
     * @param <T>         the type of the entity to retrieve
     * @param entityClass the class of the entity to retrieve
     * @return a list of all entities of the specified type
     */
    public <T> List<T> getAllEntities(Class<T> entityClass) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        String jpql = "SELECT r FROM %s r".formatted(entityClass.getSimpleName());
        TypedQuery<T> query = entityManager.createQuery(jpql, entityClass);
        return query.getResultList();
    }
}
