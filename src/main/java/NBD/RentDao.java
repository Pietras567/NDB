package NBD;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Query;

import java.util.List;

@Dao
public interface RentDao {
    @Insert
    void insert(Rent rent);

    @Select
    List<Rent> findAll();

    @Query("SELECT * FROM rents WHERE rentId = :id")
    Rent findById(long id);

    @Insert
    void update(Rent rent);

    @Delete
    void delete(Rent rent);

    @Query("SELECT * FROM rent WHERE clientId = :clientId")
    Iterable<Rent> findByClientId(long clientId);

    @Query("SELECT * FROM rent WHERE vehicleId = :vehicleId")
    Iterable<Rent> findByVehicleId(long vehicleId);
}
