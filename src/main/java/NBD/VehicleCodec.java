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

/**
 * The VehicleCodec class is responsible for encoding and decoding {@link Vehicle} objects into BSON format for use
 * with MongoDB. It supports polymorphic serialization and deserialization of different vehicle subtypes such as
 * {@link Car}, {@link Truck}, and {@link Motorbike}.
 *
 * This implementation utilizes a {@link CodecRegistry} to handle custom serialization logic for supported
 * vehicle subtypes and ensures compatibility with BSON document structure.
 */
public class VehicleCodec implements Codec<Vehicle> {
    private final CodecRegistry codecRegistry;

    public VehicleCodec(CodecRegistry codecRegistry) {
        this.codecRegistry = codecRegistry;
    }

    /**
     * Decodes a BSON document into a {@link Vehicle} object. The method expects the document
     * to represent a specific type of vehicle (Car, Truck, or Motorbike) and will decode
     * attributes unique to the respective type.
     *
     * @param reader          the {@link BsonReader} used to read the BSON document
     * @param decoderContext  the {@link DecoderContext} that defines decoding behavior
     * @return a {@link Vehicle} instance with attributes populated from the BSON document
     * @throws BsonInvalidOperationException if the BSON type is not a document
     * @throws IllegalArgumentException if the vehicle type is unknown or unsupported
     */
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

                vehicle = new Car(name, weight, power, seats);
                ((Car) vehicle).setSeats(seats);
                break;
            case "NBD.Truck":
                int loadCapacity = reader.readInt32("loadCapacity");
                name = reader.readString("name");
                power = reader.readInt32("power");
                weight = reader.readInt32("weight");

                vehicle = new Truck(name, power, weight, loadCapacity);
                ((Truck) vehicle).setLoadCapacity(loadCapacity);
                break;
            case "NBD.Motorbike":
                int engineCapacity = reader.readInt32("engineCapacity");
                name = reader.readString("name");
                power = reader.readInt32("power");
                weight = reader.readInt32("weight");

                vehicle = new Motorbike(name, power, weight, engineCapacity);
                ((Motorbike) vehicle).setEngineCapacity(engineCapacity);
                break;
            default:
                throw new IllegalArgumentException("Unknown vehicle type: " + type);
        }

        vehicle.setId(id);
        reader.readEndDocument();
        return vehicle;
    }

    /**
     * Encodes a {@link Vehicle} object into BSON format using the provided {@link BsonWriter}.
     * The method writes the vehicle's properties and determines the specific type of vehicle
     * (e.g., Car, Truck, Motorbike) based on its class. Additional fields relevant to the
     * specific vehicle type are written as well.
     *
     * @param writer        the {@link BsonWriter} used to write the BSON document
     * @param vehicle       the {@link Vehicle} instance to be encoded
     * @param encoderContext the {@link EncoderContext} to define encoding flow
     * @throws IllegalArgumentException if the vehicle's type is unknown
     */
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

        writer.writeEndDocument();
    }

    /**
     * Provides the class type that this codec can encode and decode.
     *
     * @return the {@code Class} object representing the type {@link Vehicle}.
     */
    @Override
    public Class<Vehicle> getEncoderClass() {
        return Vehicle.class;
    }
}
