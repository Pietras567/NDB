package NBD;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

/**
 * The ClientMapper interface serves as a DataStax Cassandra Data Mapper
 * for handling operations related to the Client entity. This interface
 * enables interaction with the ClientDao, providing various database
 * operations for the Client entity.
 *
 * The interface is annotated with @Mapper, marking it as a mapper
 * for Cassandra's Object Mapper framework.
 */
@Mapper
public interface ClientMapper {
    @DaoFactory
    ClientDao clientDao();
}
