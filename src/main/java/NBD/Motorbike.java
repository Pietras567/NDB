package NBD;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@BsonDiscriminator
public class Motorbike extends Vehicle {
    private int engineCapacity;

    public Motorbike(String name, int weight, int power, int engineCapacity) {
        super(name, weight, power);
        this.engineCapacity = engineCapacity;
    }

    public Motorbike() {

    }

    public int getEngineCapacity() {
        return engineCapacity;
    }

    public void setEngineCapacity(int engineCapacity) {
        this.engineCapacity = engineCapacity;
    }
}
