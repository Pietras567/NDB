package NBD;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Vehicle {
    private UUID Id;
    private String Name;
    private int Weight;
    private int Power;

    //@OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<Rent> rents = new ArrayList<>();

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
        return Id;
    }

    public int getPower() {
        return Power;
    }

    public void setPower(int power) {
        Power = power;
    }

    public Vehicle(String name, int weight, int power) {
        Name = name;
        Weight = weight;
        Power = power;
    }

    //public List<Rent> getRents() {
    //    return rents;
    //}

    //public void setRents(List<Rent> rents) {
    //    this.rents = rents;
    //}

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + Id +
                ", name='" + Name + '\'' +
                ", weight=" + Weight +
                ", Power=" + Power +
                '}';
    }
}
