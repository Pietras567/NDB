package Producer;

import com.mongodb.client.MongoDatabase;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.client.MongoCollection;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.eq;


/**
 * The RentalApi class provides operations for managing rental-related functionalities
 * such as renting and returning vehicles, as well as retrieving data from the database.
 * It interacts with a MongoDB database and utilizes a caching mechanism and message producer
 * for handling entities and events efficiently.
 */
public class RentalApi {
    private DatabaseApi databaseApi = new DatabaseApi();
    private MongoDatabase database = databaseApi.getDatabase();
    private Producer producer = new Producer(databaseApi);
    private CacheManager cacheManager = new CacheManager();

    /**
     * Handles the return process of a rented vehicle by verifying the rental status
     * and updating the associated rental record.
     *
     * @param vehicle The vehicle being returned.
     * @param client  The client attempting to return the vehicle.
     * @return {@code true} if the vehicle was successfully returned; {@code false} otherwise.
     */
    public boolean returnVehicle(Vehicle vehicle, Client client) {
        try {
            ObjectId vehicleId = vehicle.getId();
            MongoCollection<Rent> rentCollection = database.getCollection("rents", Rent.class);

            List<Rent> list = rentCollection.find(eq("vehicle_id", vehicleId)).into(new ArrayList<>());
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
                if(rent.getClient().getId().equals(client.getId())) {
                    rent.setEndDate(LocalDateTime.now());
                    cacheManager.updateEntity(rent, "rents", rent.getId());
                    producer.sendToTopic(rent); // wyslanie do teamtu zmodyfikowanego wypozyczenia
                    System.out.println("Pojazd został zwrócony");
                } else {
                    System.out.println("Pojazd byl wypozyczony przez innego klienta. Nie mozesz go zwrocic.");
                    return false;
                }
            } else {
                System.out.println("Nie można zwrócić pojazdu, nie jest on aktualnie wypożyczony");
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Handles the rental process of a vehicle for a specified client over a given number of days.
     * Verifies the availability of the vehicle and updates rental data accordingly.
     *
     * @param vehicle The vehicle being rented.
     * @param client The client renting the vehicle.
     * @param days The number of days the client wishes to rent the vehicle.
     * @return {@code true} if the vehicle was successfully rented; {@code false} if the vehicle is unavailable or an error occurs.
     */
    public boolean rent(Vehicle vehicle, Client client, int days) {
        try {
            ObjectId vehicleId = vehicle.getId();
            MongoCollection<Rent> rentCollection = database.getCollection("rents", Rent.class);
            List<Rent> list = rentCollection.find(eq("vehicle_id", vehicleId)).into(new ArrayList<>());
            boolean wypozyczony = false;
            if(!list.isEmpty()) {
                for (Rent r : list) {
                    if((LocalDateTime.now().isAfter(r.getStartDate()) || LocalDateTime.now().isEqual(r.getStartDate())) && (LocalDateTime.now().isBefore(r.getEndDate())) || LocalDateTime.now().isEqual(r.getEndDate())) {
                        System.out.println("Nie można wypożyczyć pojazdu, jest on aktualnie wypożyczony");
                        wypozyczony = true;
                    }
                }
            }

            if(!wypozyczony) {
                System.out.println("Pojazd został wypożyczony");
                Rent rent = new Rent(client.getId(), vehicle.getId(), LocalDateTime.now(), LocalDateTime.now().plusDays(days));
                cacheManager.addEntity(rent, "rents");
                producer.sendToTopic(rent); // wyslanie do tematu wypozyczenia
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Retrieves all entities of a specified type from the database.
     *
     * @param <T>         The generic type of the entities to be retrieved.
     * @param entityClass The {@code Class} object corresponding to the type of entities to retrieve.
     * @return A {@code List} containing all entities of the specified type found in the database.
     */
    public <T> List<T> getAllEntities(Class<T> entityClass) {
        MongoCollection<T> collection = database.getCollection(entityClass.getSimpleName().toLowerCase() + "s", entityClass);
        return collection.find().into(new ArrayList<>());
    }

    /**
     * Retrieves all vehicles of a specified type from the database.
     *
     * @param <T>         The type of vehicles to retrieve. This type must extend {@code Vehicle}.
     * @param entityClass The {@code Class} object corresponding to the type of vehicles to retrieve.
     * @return A {@code List} of all vehicles of the specified type found in the database.
     */
    public <T extends Vehicle> List<T> getAllVehicles(Class<T> entityClass) {
        MongoCollection<T> collection = database.getCollection(entityClass.getSimpleName().toLowerCase() + "s", entityClass);
        return collection.find().into(new ArrayList<>());
    }
}
