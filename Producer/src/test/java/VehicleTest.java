import Producer.Car;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The VehicleTest class is a unit testing class designed to test the functionality of the Car class
 * and its associated methods, particularly its constructors, getters, and setters.
 *
 * It employs JUnit testing framework to validate the expected behavior of Car instances.
 */
public class VehicleTest {
    public VehicleTest() {

    }

    @Test
    public void testConstructor() {
        Car car = new Car("Fabia",1200,100,5);
        assertEquals("Fabia", car.getName());
    }

    @Test
    public void gettersSettersTest() {
        Car car = new Car("Fabia",1200,100,5);
        assertEquals("Fabia", car.getName());
        assertEquals(1200, car.getWeight());
        assertEquals(100, car.getPower());
        assertEquals(5, car.getSeats());
        car.setSeats(2);
        car.setWeight(1000);
        assertEquals(2, car.getSeats());
        assertEquals(1000, car.getWeight());
    }
}
