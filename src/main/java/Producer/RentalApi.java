package Producer;

import com.mongodb.client.MongoDatabase;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.client.MongoCollection;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.eq;


public class RentalApi {
    private DatabaseApi databaseApi = new DatabaseApi();
    private MongoDatabase database = databaseApi.getDatabase();
    private Producer producer = new Producer(databaseApi);
    private CacheManager cacheManager = new CacheManager();



    public boolean oddaj(Vehicle vehicle, Client client) {
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


    public boolean wypozycz(Vehicle vehicle, Client client, int days) {
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

    public <T> List<T> getAllEntities(Class<T> entityClass) {
        MongoCollection<T> collection = database.getCollection(entityClass.getSimpleName().toLowerCase() + "s", entityClass);
        return collection.find().into(new ArrayList<>());
    }

    public <T extends Vehicle> List<T> getAllVehicles(Class<T> entityClass) {
        MongoCollection<T> collection = database.getCollection(entityClass.getSimpleName().toLowerCase() + "s", entityClass);
        return collection.find().into(new ArrayList<>());
    }
}
