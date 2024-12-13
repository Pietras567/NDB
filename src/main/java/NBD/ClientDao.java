package NBD;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;

import java.util.List;
import java.util.UUID;

@Dao
public interface ClientDao {
    @Insert
    void insert(Client client);

    @Select
    List<Client> findAll();

    @Select
    Client findById(UUID id);

    @Insert
    void update(Client client);

    @Delete
    void delete(Client client);
}
