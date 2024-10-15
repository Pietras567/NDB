package NBD;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Rent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "client_id", insertable = false, updatable = false)
    private long client_id;
    @Column(name = "vehicle_id", insertable = false, updatable = false)
    private long vehicle_id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Rent(long client_id, long vehicle_id, LocalDateTime startDate, LocalDateTime endDate) {
        this.client_id = client_id;
        this.vehicle_id = vehicle_id;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

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
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
