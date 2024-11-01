package NBD;


import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonId;

import static com.mongodb.client.model.Filters.eq;

public class Client {
    @BsonId
    private ObjectId Id;
    @BsonProperty("name")
    private String name;
    @BsonProperty("age")
    private int age;

    public Client(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Client() {}


    public ObjectId getId() {
        return Id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<Rent> allRents() {
        DatabaseApi api = new DatabaseApi();
        MongoDatabase database = api.getDatabase();
        MongoCollection<Rent> collection = database.getCollection("rents", Rent.class);
        return collection.find(eq("client_id", Id)).into(new ArrayList<>());
    }


    @Override
    public String toString() {
        return "Client{" +
                "id=" + Id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", rents=" + this.allRents() +
                '}';
    }

    public void setId(ObjectId id) {
        this.Id = id;
    }
}
