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
