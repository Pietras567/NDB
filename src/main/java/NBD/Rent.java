package NBD;

import java.time.LocalDateTime;
import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class Rent {
    @BsonId
    private ObjectId id;
    @BsonProperty("client_id")
    private ObjectId client_id;
    @BsonProperty("vehicle_id")
    private ObjectId vehicle_id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Rent(ObjectId client_id, ObjectId vehicle_id, LocalDateTime startDate, LocalDateTime endDate) {
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

    public ObjectId getId() {
        return id;
    }


    public ObjectId getClient_id() {
        return client_id;
    }

    public void setClient_id(ObjectId client_id) {
        this.client_id = client_id;
    }

    public ObjectId getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(ObjectId vehicle_id) {
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
        DatabaseApi api = new DatabaseApi();
        return api.getEntity(Vehicle.class, "vehicles", vehicle_id);
    }

    public Client getClient() {
        DatabaseApi api = new DatabaseApi();
        return api.getEntity(Client.class, "clients", client_id);
    }
}
