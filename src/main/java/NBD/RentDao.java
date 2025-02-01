package NBD;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Update;

import java.util.List;
import java.util.UUID;

/**
 * Data Access Object (DAO) interface for managing rent-related operations in the database.
 * Provides methods for inserting, retrieving, updating, and deleting rent records.
 * It also supports querying rents by specific fields such as client ID or vehicle ID.
 */
@Dao
public interface RentDao {
    @Insert
    void insert(Rent rent);

    @Select
    PagingIterable<Rent> findAll();

    @Query("SELECT * FROM rents WHERE rent_id = :id")
    Rent findById(UUID id);

    @Update
    void update(Rent rent);

    @Delete
    void delete(Rent rent);

    @Query("SELECT * FROM rents WHERE (client_id = :clientId) ALLOW FILTERING")
    PagingIterable<Rent> findByClientId(UUID clientId);

    @Query("SELECT * FROM rents WHERE (vehicle_id = :vehicleId) ALLOW FILTERING")
    PagingIterable<Rent> findByVehicleId(UUID vehicleId);

    @Query("SELECT * FROM rents WHERE vehicle_id = :vehicleId AND client_id = :clientId ALLOW FILTERING")
    PagingIterable<Rent> findByVehicleClientId(UUID vehicleId, UUID clientId);
}
