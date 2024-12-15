package NBD;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import java.util.UUID;


public abstract class Vehicle {
    @PartitionKey
    @CqlName("vehicle_id")
    private UUID id;

    @CqlName("name")
    private String Name;

    @CqlName("weight")
    private int Weight;

    @CqlName("power")
    private int Power;

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

    public UUID getId() {
        return id;
    }

    public int getPower() {
        return Power;
    }

    public void setPower(int power) {
        Power = power;
    }

    public Vehicle(String name, int weight, int power) {
        this.id = UUID.randomUUID();
        Name = name;
        Weight = weight;
        Power = power;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", name='" + Name + '\'' +
                ", weight=" + Weight +
                ", Power=" + Power +
                '}';
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
