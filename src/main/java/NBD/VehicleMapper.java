package NBD;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface VehicleMapper {
    @DaoFactory
    VehicleDao vehicleDao();
}
