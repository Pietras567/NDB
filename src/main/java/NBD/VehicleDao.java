package NBD;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.ResultSet;
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
    PagingIterable<Car> findAllCar();

    @Select
    PagingIterable<Motorbike> findAllMotorbike();

    @Select
    PagingIterable<Truck> findAllTruck();

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
