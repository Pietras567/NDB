package NBD;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Motorbike")
public class Motorbike extends Vehicle {
    private int engineCapacity;

    public Motorbike(String name, int weight, int power, int engineCapacity) {
        super(name, weight, power);
        this.engineCapacity = engineCapacity;
    }

    public int getEngineCapacity() {
        return engineCapacity;
    }

    public void setEngineCapacity(int engineCapacity) {
        this.engineCapacity = engineCapacity;
    }
}
