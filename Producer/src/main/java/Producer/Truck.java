package Producer;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@BsonDiscriminator
public class Truck extends Vehicle {
    private int loadCapacity;

    public Truck(String name, int weight, int power, int loadCapacity) {
        super(name, weight, power);
        this.loadCapacity = loadCapacity;
    }

    public Truck() {

    }

    public int getLoadCapacity() {
        return loadCapacity;
    }

    public void setLoadCapacity(int loadCapacity) {
        this.loadCapacity = loadCapacity;
    }
}
