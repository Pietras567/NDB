package NBD;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Car")
public class Car extends Vehicle {
    private int Seats;

    public Car(String name, int weight, int power, int seats) {
        super(name, weight, power);
        Seats = seats;
    }

    public int getSeats() {
        return Seats;
    }

    public void setSeats(int seats) {
        Seats = seats;
    }
}
