package Producer;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonId;
import static com.mongodb.client.model.Filters.eq;

public abstract class Vehicle {
    @BsonId
    private ObjectId Id;
    private String Name;
    private int Weight;
    private int Power;
    private int rentalId;

    public Vehicle() {

    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getWeight() {
        return Weight;
    }

    public void setWeight(int weight) {
        Weight = weight;
    }

    public ObjectId getId() {
        return Id;
    }

    public int getPower() {
        return Power;
    }

    public void setPower(int power) {
        Power = power;
    }

    public int getRentalId() {
        return rentalId;
    }

    public void setRentalId(int rentalId) {
        this.rentalId = rentalId;
    }

    public Vehicle(String name, int weight, int power) {
        Name = name;
        Weight = weight;
        Power = power;
    }

    public List<Rent> allRents() {
        DatabaseApi api = new DatabaseApi();
        MongoDatabase database = api.getDatabase();
        MongoCollection<Rent> collection = database.getCollection("rents", Rent.class);
        return collection.find(eq("vehicle_id", this.Id)).into(new ArrayList<>());
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + Id.toString().replaceFirst("^0+(?!$)", "") +
                ", name='" + Name + '\'' +
                ", weight=" + Weight +
                ", Power=" + Power +
                '}';
    }

    public void setId(ObjectId id) {
        this.Id = id;
    }
}
