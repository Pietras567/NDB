package NBD;

import com.datastax.oss.driver.api.mapper.annotations.*;

import java.util.List;
import java.util.UUID;

@Dao
public interface VehicleDao {
    @Insert
    void insert(Car car);

    @Insert
    void insert(Truck truck);

    @Insert
    void insert(Motorbike motorbike);

    @Select
    Iterable<Car> findAllCar();

    @Select
    Iterable<Motorbike> findAllMotorbike();

    @Select
    Iterable<Truck> findAllTruck();

    @Select
    Car findCarById(UUID id);

    @Select
    Motorbike findMotorbikeById(UUID id);

    @Select
    Truck findTruckById(UUID id);

    @Update
    void update(Car car);

    @Update
    void update(Truck truck);

    @Update
    void update(Motorbike motorbike);

    @Delete
    void delete(Car car);

    @Delete
    void delete(Truck truck);

    @Delete
    void delete(Motorbike motorbike);
}
