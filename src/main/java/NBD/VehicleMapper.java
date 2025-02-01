package NBD;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

/**
 * Provides the mapping interface for handling vehicle-related data operations in the database.
 * This mapper is responsible for creating a Data Access Object (DAO) instance for managing
 * vehicle entities such as cars, trucks, and motorbikes.
 *
 * The mapper encapsulates the following functionalities:
 * - Extends the functionality of the DataStax Cassandra Mapper framework.
 * - Allows interaction with the database through the {@link VehicleDao} for CRUD operations
 *   and other queries related to vehicle data.
 */
@Mapper
public interface VehicleMapper {
    @DaoFactory
    VehicleDao vehicleDao();
}
