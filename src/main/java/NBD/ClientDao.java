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
 * This interface provides Data Access Object (DAO) methods for interacting with the
 * Client entity in the Cassandra database using the DataStax Mapper framework.
 *
 * Methods in this interface allow for basic CRUD operations: creating, reading, updating,
 * and deleting Client objects in the database. Additionally, the findAll method supports
 * retrieving multiple Client records.
 *
 * The methods are integrated with the database via annotations such as @Insert, @Select,
 * @Update, @Delete, and @Query, which map the operations to the corresponding Cassandra queries.
 *
 * The Client entity represents a client record stored in the database. It includes
 * attributes such as UUID (client_id), name, and age.
 */
@Dao
public interface ClientDao {
    @Insert
    void insert(Client client);

    @Select
    PagingIterable<Client> findAll();

    @Query("SELECT * FROM clients WHERE client_id = :client_id")
    Client findById(UUID client_id);

    @Update
    void update(Client client);

    @Delete
    void delete(Client client);
}
