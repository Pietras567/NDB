package Producer;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;


/**
 * The DatabaseApi Class provides methods to perform CRUD (Create, Retrieve, Update, Delete) operations
 * on MongoDB collections. It implements the CRUDManager interface to manage entities within a MongoDB database.
 *
 * This class uses a replica set MongoDB connection and handles BSON encoding/decoding using custom
 * codec registries. It ensures atomicity by utilizing MongoDB transactions with sessions.
 *
 * The class also manages ID sequences for collections by initializing counters and auto-incrementing
 * them for collections such as "clients", "vehicles", and "rents".
 */
public class DatabaseApi implements CRUDManager {
    private final static ConnectionString connectionString = new ConnectionString(
            "mongodb://mongo1:27017,mongo2:27018,mongo3:27019/?replicaSet=replica_set_single"
    );

    private final static MongoCredential credential = MongoCredential.createCredential(
            "admin", "admin", "adminpassword".toCharArray());

    private final static CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(
            PojoCodecProvider.builder()
                    .automatic(true)
                    .build());

    private final static CodecRegistry customCodecRegistry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(new BsonValueCodecProvider(), new DocumentCodecProvider()),
            CodecRegistries.fromCodecs(new VehicleCodec(pojoCodecRegistry)),
            pojoCodecRegistry
    );

    private final static MongoClientSettings clientSettings = MongoClientSettings.builder()
            .credential(credential)
            .applyConnectionString(connectionString)
            .codecRegistry(customCodecRegistry)
            .build();
    private final static MongoClient mongoClient = MongoClients.create(clientSettings);
    private final static MongoDatabase mongoDatabase = mongoClient.getDatabase("admin");

    public static MongoDatabase getDatabase() {
        return mongoDatabase;
    }

    public static MongoClient getClient() {
        return mongoClient;
    }

    /**
     * Ensures that a counter for a specific sequence name exists in the database.
     * If the counter does not exist, it initializes it with a starting value of 0.
     *
     * @param sequenceName the name of the sequence for which the counter should be initialized
     */
    private void initializeCounterIfNotExists(String sequenceName) {
        if (!mongoDatabase.listCollectionNames().into(new ArrayList<>()).contains("counters")) {
            mongoDatabase.createCollection("counters");
        }

        MongoCollection<Document> counters = mongoDatabase.getCollection("counters");
        Document existingCounter = counters.find(eq("_id", sequenceName)).first();

        if (existingCounter == null) {
            counters.insertOne(new Document("_id", sequenceName).append("sequence_value", 0));
        }
    }

    /**
     * Retrieves the next value in a sequential counter stored in the "counters" collection within the MongoDB database.
     * The counter is incremented atomically and the updated value is returned.
     *
     * @param sequenceName the name of the sequence for which the next value is requested
     * @return the next integer value of the specified sequence
     */
    private int getNextSequenceValue(String sequenceName) {
        MongoCollection<Document> counters = mongoDatabase.getCollection("counters");
        Document sequenceDocument = counters.findOneAndUpdate(
                Filters.eq("_id", sequenceName),
                Updates.inc("sequence_value", 1)
        );
        return sequenceDocument.getInteger("sequence_value");
    }

    /**
     * Constructs a new instance of the DatabaseApi class.
     * Initializes specific counters in the database if they do not already exist.
     * This ensures that sequence counters for "clients", "vehicles", and "rents" collections
     * are properly set up for use in managing unique identifiers or sequences.
     */
    public DatabaseApi() {
        initializeCounterIfNotExists("clients");
        initializeCounterIfNotExists("vehicles");
        initializeCounterIfNotExists("rents");
    }

    /**
     * Adds a new entity to the specified collection in the database.
     * This method ensures the entity is assigned a unique identifier using a sequence counter
     * and performs the operation within a transaction ensuring atomicity.
     *
     * @param <T>            the type of the entity being added
     * @param entity         the entity object to persist in the database
     * @param collectionName the name of the collection where the entity will be stored
     * @throws IllegalArgumentException if the specified collection name is not recognized
     */
    @Override
    public <T> void addEntity(T entity, String collectionName) {
        try (ClientSession session = getClient().startSession()){  // ATOMICITY
            session.startTransaction();
            MongoCollection<T> collection = mongoDatabase.getCollection(collectionName, (Class<T>) entity.getClass());

            long id;
            switch (collectionName) {
                case "clients":
                    id = getNextSequenceValue("clients");
                    break;
                case "vehicles":
                    id = getNextSequenceValue("vehicles");
                    break;
                case "rents":
                    id = getNextSequenceValue("rents");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown collection: " + collectionName);
            }
            entity.getClass().getMethod("setId", ObjectId.class).invoke(entity, new ObjectId(String.format("%024x", id)));
            collection.insertOne(session, entity);
            session.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes an entity from the specified collection in the database. The operation is
     * performed within a transaction to ensure atomicity and consistency.
     *
     * @param <T>            the type of the entity to be deleted
     * @param entityClass    the class type of the entity
     * @param collectionName the name of the MongoDB collection from which the entity is to be deleted
     * @param id             the unique identifier of the entity to be deleted
     */
    @Override
    public <T> void deleteEntity(Class<T> entityClass, String collectionName, ObjectId id) { // JAKO PARAMETR PODAJEMY np. Vehicle.class
        try (ClientSession session = getClient().startSession()) {
            session.startTransaction();
            MongoCollection<T> collection = mongoDatabase.getCollection(collectionName, entityClass);
            collection.deleteOne(session, eq("_id", id));
            session.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing entity in the specified MongoDB collection.
     * This method performs the operation within a transaction
     * to ensure atomicity and consistency of the data.
     *
     * @param <T>            the type of the entity being updated
     * @param entity         the updated entity object to be persisted in the database
     * @param collectionName the name of the MongoDB collection where the entity is stored
     * @param id             the unique identifier of the entity to update
     */
    @Override
    public <T> void updateEntity(T entity, String collectionName, ObjectId id) {
        try (ClientSession session = getClient().startSession()) {
            session.startTransaction();
            MongoCollection<T> collection = mongoDatabase.getCollection(collectionName, (Class<T>) entity.getClass());
            collection.replaceOne(session, eq("_id", id), entity);
            session.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves an entity from the specified MongoDB collection by its unique identifier.
     * The operation is performed within a transaction to ensure atomicity and consistency.
     *
     * @param <T>            the type of the entity to retrieve
     * @param entityClass    the class type of the entity being retrieved
     * @param collectionName the name of the MongoDB collection from which to retrieve the entity
     * @param id             the unique identifier of the entity to retrieve
     * @return the entity of the specified type if found, or null if no matching entity exists
     */
    @Override
    public <T> T getEntity(Class<T> entityClass, String collectionName, ObjectId id) {
        T entity = null;
        try (ClientSession session = getClient().startSession()) {
            session.startTransaction();
            MongoCollection<T> collection = mongoDatabase.getCollection(collectionName, entityClass);
            entity = collection.find(session, eq("_id", id)).first();
            session.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }
}
