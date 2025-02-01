package NBD;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

/**
 * Mapper interface for creating and providing access to the DAO implementation
 * for the 'rents' table in the database. This mapper acts as a bridge between
 * the application and the database by defining a factory method for the RentDao.
 *
 * The `RentDao` includes methods for various database operations, such as
 * inserting, updating, deleting, and querying rents data.
 *
 * This mapper utilizes a `Mapper` annotation for dependency injection and
 * is generated at runtime to provide the DAO implementation.
 */
@Mapper
public interface RentMapper {
    @DaoFactory
    RentDao rentDao();
}
