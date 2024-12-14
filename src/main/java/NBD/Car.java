package NBD;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;

@Entity
@CqlName("cars")
public class Car extends Vehicle {
    @CqlName("seats")
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

    @Override
    public String toString() {
        return "Car{" +
                "Seats=" + Seats +
                "} " + super.toString();
    }
}
