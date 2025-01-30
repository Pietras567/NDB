package NBD;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.MessageFormatter;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.internals.Topic;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.bson.Document;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;


public class DatabaseApi implements CRUDManager {
    private final static ConnectionString connectionString = new ConnectionString(
            "mongodb://mongo1:27017,mongo2:27018,mongo3:27019/?replicaSet=replica_set_single"
    );

    private final static MongoCredential credential = MongoCredential.createCredential(
            "admin", "admin", "adminpassword".toCharArray());


    private final static CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(
            PojoCodecProvider.builder()
                    .automatic(true)
                    //.conventions(List.of(Conventions.ANNOTATION_CONVENTION)) // dodana ta opcja
                    .build());


    private final static CodecRegistry customCodecRegistry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(new BsonValueCodecProvider(), new DocumentCodecProvider()),
            CodecRegistries.fromCodecs(new VehicleCodec(pojoCodecRegistry)),
            pojoCodecRegistry
    );


    private final static MongoClientSettings clientSettings = MongoClientSettings.builder()
            .credential(credential)
            //.applyToClusterSettings(builder -> builder.applySettings(clusterSettings))
            .applyConnectionString(connectionString)
            .codecRegistry(customCodecRegistry)
            //.codecRegistry(codecRegistry)
            .build();
    private final static MongoClient mongoClient = MongoClients.create(clientSettings);
    private final static MongoDatabase mongoDatabase = mongoClient.getDatabase("admin");

    public static MongoDatabase getDatabase() {
        return mongoDatabase;
    }

    public static MongoClient getClient() {
        return mongoClient;
    }

    // Inicjalizacja licznika dla kolekcji, jeśli nie istnieje
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

    // Pobierz kolejną wartość ID
    private int getNextSequenceValue(String sequenceName) {
        MongoCollection<Document> counters = mongoDatabase.getCollection("counters");
        Document sequenceDocument = counters.findOneAndUpdate(
                Filters.eq("_id", sequenceName),
                Updates.inc("sequence_value", 1)
        );
        return sequenceDocument.getInteger("sequence_value");
    }

    public DatabaseApi() {
        initializeCounterIfNotExists("clients");
        initializeCounterIfNotExists("vehicles");
        initializeCounterIfNotExists("rents");
    }

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
