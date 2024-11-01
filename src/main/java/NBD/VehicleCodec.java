package NBD;
import org.bson.BsonInvalidOperationException;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

public class VehicleCodec implements Codec<Vehicle> {
    private final CodecRegistry codecRegistry;

    public VehicleCodec(CodecRegistry codecRegistry) {
        this.codecRegistry = codecRegistry;
    }

    @Override
    public Vehicle decode(BsonReader reader, DecoderContext decoderContext) {
        if (reader.getCurrentBsonType() != BsonType.DOCUMENT) {
            throw new BsonInvalidOperationException("Expected DOCUMENT but found " + reader.getCurrentBsonType());
        }

        reader.readStartDocument();

        ObjectId id = reader.readObjectId("_id");
        String type = reader.readString("_t");

        String name;
        int power;
        int weight;

        Vehicle vehicle;
        switch (type) {
            case "NBD.Car":
                name = reader.readString("name");
                power = reader.readInt32("power");
                int seats = reader.readInt32("seats");
                weight = reader.readInt32("weight");

                vehicle = new Car(name, power, seats, weight);
                //vehicle = codecRegistry.get(Car.class).decode(reader, decoderContext);
                ((Car) vehicle).setSeats(seats);
                break;
            case "NBD.Truck":
                int loadCapacity = reader.readInt32("loadCapacity");
                name = reader.readString("name");
                power = reader.readInt32("power");
                weight = reader.readInt32("weight");

                vehicle = new Truck(name, power, weight, loadCapacity);
                //vehicle = codecRegistry.get(Truck.class).decode(reader, decoderContext);
                ((Truck) vehicle).setLoadCapacity(loadCapacity);
                break;
            case "NBD.Motorbike":
                int engineCapacity = reader.readInt32("engineCapacity");
                name = reader.readString("name");
                power = reader.readInt32("power");
                weight = reader.readInt32("weight");

                vehicle = new Motorbike(name, power, weight, engineCapacity);
                //vehicle = codecRegistry.get(Motorbike.class).decode(reader, decoderContext);
                ((Motorbike) vehicle).setEngineCapacity(engineCapacity);
                break;
            default:
                throw new IllegalArgumentException("Unknown vehicle type: " + type);
        }

        vehicle.setId(id);
        reader.readEndDocument();
        return vehicle;
    }

    @Override
    public void encode(BsonWriter writer, Vehicle vehicle, EncoderContext encoderContext) {
        writer.writeStartDocument();

        if (vehicle.getId() != null) {
            writer.writeObjectId("_id", vehicle.getId());
        }
        writer.writeString("_t", vehicle.getClass().getSimpleName().toLowerCase());



        switch (vehicle.getClass().getSimpleName()) {
            case "Car":
                writer.writeString("name", ((Car) vehicle).getName());
                writer.writeInt32("power", ((Car) vehicle).getPower());
                writer.writeInt32("seats", ((Car) vehicle).getSeats());
                writer.writeInt32("weight", ((Car) vehicle).getWeight());
                break;
            case "Truck":
                writer.writeString("name", ((Truck) vehicle).getName());
                writer.writeInt32("power", ((Truck) vehicle).getPower());
                writer.writeInt32("weight", ((Truck) vehicle).getWeight());
                writer.writeInt32("loadCapacity", ((Truck) vehicle).getLoadCapacity());
                break;
            case "Motorbike":
                writer.writeString("name", ((Motorbike) vehicle).getName());
                writer.writeInt32("power", ((Motorbike) vehicle).getPower());
                writer.writeInt32("weight", ((Motorbike) vehicle).getWeight());
                writer.writeInt32("engineCapacity", ((Motorbike) vehicle).getEngineCapacity());
                break;
            default:
                throw new IllegalArgumentException("Unknown vehicle type: " + vehicle.getClass().getSimpleName());
        }





        // Rzutowanie kodera na typ Vehicle z wykorzystaniem CodecRegistry
        //Codec<? super Vehicle> codec = (Codec<? super Vehicle>) codecRegistry.get(vehicle.getClass());
        //codec.encode(writer, vehicle, encoderContext);  // Użyj zakodowanego obiektu

        writer.writeEndDocument();
    }

    @Override
    public Class<Vehicle> getEncoderClass() {
        return Vehicle.class;
    }
}
