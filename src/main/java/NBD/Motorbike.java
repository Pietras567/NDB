package NBD;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;

@Entity
@CqlName("motorbikes")
public class Motorbike extends Vehicle {
    @CqlName("engine_capacity")
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
