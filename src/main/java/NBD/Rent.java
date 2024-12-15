package NBD;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import java.time.LocalDateTime;
import java.util.UUID;
import java.time.Instant;

@Entity
@CqlName("rents")
public class Rent {
    @PartitionKey
    @CqlName("rent_id")
    private UUID id;

    @CqlName("client_id")
    private UUID client_id;

    @CqlName("vehicle_id")
    private UUID vehicle_id;

    @ClusteringColumn(0)
    @CqlName("start_date")
    private Instant startDate;

    @ClusteringColumn(1)
    @CqlName("end_date")
    private Instant endDate;

    public Rent(UUID client_id, UUID vehicle_id, Instant startDate, Instant endDate) {
        this.id = UUID.randomUUID();
        this.client_id = client_id;
        this.vehicle_id = vehicle_id;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Rent() {

    }

    @Override
    public String toString() {
        return "Rent{" +
                "id=" + id +
                ", client_id=" + client_id +
                ", vehicle_id=" + vehicle_id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }

    public UUID getId() {
        return id;
    }

    public UUID getClient_id() {
        return client_id;
    }

    public void setClient_id(UUID client_id) {
        this.client_id = client_id;
    }

    public UUID getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(UUID vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Vehicle getVehicle() {
        return null; ///todo pobieranie z bazy
    }

    public Client getClient() {
        return null; ///todo pobieranie z bazy
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
