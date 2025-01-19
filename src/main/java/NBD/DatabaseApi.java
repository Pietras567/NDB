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

    //Properties properties = new Properties();

    public static void createTopic() {
        final String ordersTopic = "rents";
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        try (final AdminClient adminClient = AdminClient.create(props)) {
            try {
                // Define topic
                NewTopic newTopic = new NewTopic(ordersTopic, 3, (short)1);

                // Create topic, which is async call.
                final CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(newTopic));

                // Since the call is Async, Lets wait for it to complete.
                createTopicsResult.values().get(ordersTopic).get();
            } catch (InterruptedException | ExecutionException e) {
                if (!(e.getCause() instanceof TopicExistsException))
                    throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    public List<KafkaConsumer<UUID, String>> createConsumerGroup() {
        List<KafkaConsumer<UUID, String>> consumers = new ArrayList<>();

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "rents");
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");

        //for (int i = 0; i <= 2; i++) {
            KafkaConsumer<UUID, String> consumer = new KafkaConsumer<>(consumerProps);
            consumer.subscribe(List.of("rents"));
            consumers.add(consumer);
        //}
        return consumers;
    }

    public void consume(KafkaConsumer<UUID, String> consumer) {
        try {
            MessageFormat formatter = new MessageFormat("Temat {0}, partition {1}, offset {2, number, integer}, klucz {3}, wartosc {4}");
            while (true) {
                Duration duration = Duration.ofMillis(100);
                ConsumerRecords<UUID, String> records = consumer.poll(duration);

                for (ConsumerRecord<UUID, String> record : records) {
                    String result = formatter.format(new Object[]{record.topic(), record.partition(), record.offset(), record.key(), record.value()});
                    System.out.println(result);
                }
                consumer.commitSync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        createTopic();
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
