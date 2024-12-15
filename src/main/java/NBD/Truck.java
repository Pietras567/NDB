package NBD;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;

@Entity
@CqlName("trucks")
public class Truck extends Vehicle {
    @CqlName("load_capacity")
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

    @Override
    public String toString() {
        return "Truck{" +
                "loadCapacity=" + loadCapacity +
                "} " + super.toString();
    }
}
