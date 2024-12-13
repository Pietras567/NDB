package NBD;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Update;

import java.util.List;
import java.util.UUID;

@Dao
public interface RentDao {
    @Insert
    void insert(Rent rent);

    @Select
    List<Rent> findAll();

    @Query("SELECT * FROM rents WHERE rent_id = :id")
    Rent findById(long id);

    @Update
    void update(Rent rent);

    @Delete
    void delete(Rent rent);

    @Query("SELECT * FROM rent WHERE client_id = :clientId")
    Iterable<Rent> findByClientId(long clientId);

    @Query("SELECT * FROM rent WHERE vehicle_id = :vehicleId")
    Iterable<Rent> findByVehicleId(long vehicleId);

    @Query("SELECT * FROM rent WHERE vehicle_id = :vehicleId AND client_id = :clientId")
    Iterable<Rent> findByVehicleClientId(long vehicleId, long clientId);
}
