package NBD;

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

    public boolean oddaj(Vehicle vehicle, Client client) {
        try {
            ObjectId vehicleId = vehicle.getId();
            System.out.println(vehicleId);
            MongoCollection<Rent> rentCollection = database.getCollection("rents", Rent.class);

            List<Rent> list = rentCollection.find(eq("vehicle_id", vehicleId)).into(new ArrayList<>());
            boolean wypozyczony = false;
            Rent rent = null;
            if(!list.isEmpty()) {
                for (Rent r : list) {
                    if((LocalDateTime.now().isAfter(r.getStartDate()) || LocalDateTime.now().isEqual(r.getStartDate())) && (LocalDateTime.now().isBefore(r.getEndDate())) || LocalDateTime.now().isEqual(r.getEndDate())) {
                        System.out.println("zwracamy");
                        wypozyczony = true;
                        rent = r;
                    }
                }
            }

            if(wypozyczony) {
                if(rent.getClient().getId() == client.getId()) {
                    rent.setEndDate(LocalDateTime.now());
                    databaseApi.updateEntity(rent, "rents", rent.getId());
                } else {
                    System.out.println("Pojazd byl wypozyczony przez innego klienta. Nie mozesz go zwrocic.");
                    return false;
                }
            } else {
                System.out.println("nie zwracamy");
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
            System.out.println(vehicleId);
            MongoCollection<Rent> rentCollection = database.getCollection("rents", Rent.class);
            List<Rent> list = rentCollection.find(eq("vehicle_id", vehicleId)).into(new ArrayList<>());
            boolean wypozyczony = false;
            if(!list.isEmpty()) {
                for (Rent r : list) {
                    if((LocalDateTime.now().isAfter(r.getStartDate()) || LocalDateTime.now().isEqual(r.getStartDate())) && (LocalDateTime.now().isBefore(r.getEndDate())) || LocalDateTime.now().isEqual(r.getEndDate())) {
                        System.out.println("nie jadymy");
                        wypozyczony = true;
                    }
                }
            }

            if(!wypozyczony) {
                System.out.println("jadymy");
                Rent rent = new Rent(client.getId(), vehicle.getId(), LocalDateTime.now(), LocalDateTime.now().plusDays(days));
                //rent.setClient(client);
                //rent.setVehicle(vehicle);
                databaseApi.addEntity(rent, "rents");
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
}
