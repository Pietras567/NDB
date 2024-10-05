package NBD;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Truck")
public class Truck extends Vehicle {
    private int loadCapacity;

    public Truck(String name, int weight, int power, int loadCapacity) {
        super(name, weight, power);
        this.loadCapacity = loadCapacity;
    }

    public int getLoadCapacity() {
        return loadCapacity;
    }

    public void setLoadCapacity(int loadCapacity) {
        this.loadCapacity = loadCapacity;
    }
}
