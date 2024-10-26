package NBD;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import com.mongodb.connection.ClusterSettings;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


public class DatabaseApi implements CRUDManager {
    //private final static ClusterSettings clusterSettings = ClusterSettings.builder()
    //        .hosts(Collections.singletonList(new ServerAddress("mongo_primary", 27017)))  // Adres MongoDB
    //        .requiredReplicaSetName("rs0")
    //        .build();
    private final static ConnectionString connectionString = new ConnectionString("mongodb://mongo_primary:27017,mongo_secondary1:27018,mongo_secondary2:27019/replicaSet=rs0");
    private final static MongoCredential credential = MongoCredential.createCredential("nbd", "admin", "nbdpassword".toCharArray());
    private final static CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
    private final static CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
    private final static MongoClientSettings clientSettings = MongoClientSettings.builder()
            .credential(credential)
            //.applyToClusterSettings(builder -> builder.applySettings(clusterSettings))
            .applyConnectionString(connectionString)
            .codecRegistry(codecRegistry)
            .build();
    private final static MongoClient mongoClient = MongoClients.create(clientSettings);
    private final static MongoDatabase mongoDatabase = mongoClient.getDatabase("admin");

    private static final AtomicLong clientCounter = new AtomicLong(1);
    private static final AtomicLong vehicleCounter = new AtomicLong(1);
    private static final AtomicLong rentCounter = new AtomicLong(1);

    public static MongoDatabase getDatabase() {
        return mongoDatabase;
    }

    public static MongoClient getClient() {
        return mongoClient;
    }

    public DatabaseApi() {
        //getDatabase().createCollection("vehicles");
        //getDatabase().createCollection("rents");
        //getDatabase().createCollection("clients");
    }

    @Override
    public <T> void addEntity(T entity, String collectionName) {
        try (ClientSession session = getClient().startSession()){  // ATOMICITY
            session.startTransaction();
            MongoCollection<T> collection = mongoDatabase.getCollection(collectionName, (Class<T>) entity.getClass());

            long id;
            switch (collectionName) {
                case "clients":
                    id = clientCounter.getAndIncrement();
                    break;
                case "vehicles":
                    id = vehicleCounter.getAndIncrement();
                    break;
                case "rents":
                    id = rentCounter.getAndIncrement();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown collection: " + collectionName);
            }
            System.out.println(id);
            System.out.println(String.valueOf(id));
            System.out.println(new ObjectId(String.format("%024x", id)));
            entity.getClass().getMethod("setId", ObjectId.class).invoke(entity, new ObjectId(String.format("%024x", id)));
            collection.insertOne(session, entity);
            session.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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
