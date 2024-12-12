package NBD;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import java.time.LocalDateTime;

@Entity
public class Rent {
    @PartitionKey
    private long id;

    @Column(name = "clientId")
    private long client_id;

    @Column(name = "vehicleId")
    private long vehicle_id;

    @ClusteringColumn
    @Column(name = "startDate")
    private LocalDateTime startDate;

    @ClusteringColumn
    @Column(name = "endDate")
    private LocalDateTime endDate;

    public Rent(long client_id, long vehicle_id, LocalDateTime startDate, LocalDateTime endDate) {
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

    public long getId() {
        return id;
    }


    public long getClient_id() {
        return client_id;
    }

    public void setClient_id(long client_id) {
        this.client_id = client_id;
    }

    public long getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(long vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Vehicle getVehicle() {
        return null; ///todo pobieranie z bazy
    }

    public Client getClient() {
        return null; ///todo pobieranie z bazy
    }
}
