package NBD;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@BsonDiscriminator
public class Car extends Vehicle {
    private int Seats;

    public Car(String name, int weight, int power, int seats) {
        super(name, weight, power);
        Seats = seats;
    }

    public Car() {

    }

    public int getSeats() {
        return Seats;
    }

    public void setSeats(int seats) {
        Seats = seats;
    }
}
