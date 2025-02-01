package Producer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bson.types.ObjectId;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.UUID;

/**
 * The Producer class is responsible for creating and managing Kafka topics as well as sending rental information data to a specific Kafka topic.
 * It integrates with a database to fetch necessary information to format messages appropriately before publishing them to the topic.
 */
public class Producer {
    private static final String[] RENTAL_CENTERS = {"CarRental", "JadymyRental", "ZygzakMcQueen"};
    private DatabaseApi databaseApi;

    /**
     * Constructs a new Producer instance with the specified DatabaseApi.
     * Initializes the database API and creates the topic for processing rents.
     *
     * @param databaseApi The DatabaseApi instance used for database operations.
     */
    public Producer(DatabaseApi databaseApi) {
        this.databaseApi = databaseApi;
        createTopic();
    }

    /**
     * Creates a Kafka topic named "rents" if it does not already exist.
     * The topic will have a configuration of 3 partitions and a replication factor of 1.
     * This method uses the Kafka AdminClient API for topic management.
     *
     * It ensures the topic creation via an asynchronous API call and waits
     * for the completion of the operation. If the topic already exists, the exception
     * will be caught and suppressed; otherwise, any other exceptions will be thrown.
     *
     * Exceptions:
     * - Throws a RuntimeException if any error occurs during the topic creation,
     *   except for TopicExistsException.
     */
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

    /**
     * Sends rent information to a Kafka topic named "rents". This method serializes
     * rent details into JSON format and uses a Kafka producer to send the data.
     * The method ensures transactionality and retries the operation in case of
     * transient errors.
     *
     * @param rent The Rent object containing the details of the rental transaction.
     *             Includes client, vehicle, and rental period information.
     */
    public void sendToTopic(Rent rent) {
        // create Producer properties
        String bootstrapServers = "127.0.0.1:9092";
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "4da972e7-ae0a-4e28-b133-1321007663a4");

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // create a producer record
        ObjectId rentID = rent.getId();
        String clientName = databaseApi.getEntity(Client.class, "clients", rent.getClient_id()).getName();
        String vehicleName = databaseApi.getEntity(Vehicle.class, "vehicles", rent.getVehicle_id()).getName();
        LocalDateTime startTime = rent.getStartDate();
        LocalDateTime endTime = rent.getEndDate();

        int rentalId = databaseApi.getEntity(Vehicle.class, "vehicles", rent.getVehicle_id()).getRentalId();
        String rentalCenter = RENTAL_CENTERS[rentalId];

        String rentInfo = String.format("{\"rentID\": %s, \"rental_center\": \"%s\", \"clientName\": \"%s\", \"vehicleName\": \"%s\", \"rental_time\": \"%s\"}",
                rentID, rentalCenter, clientName, vehicleName, startTime);

        producer.initTransactions();
        try {
            producer.beginTransaction();
            boolean sent = false;
            int attempt = 0;
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>("rents", toUUID(rent.getId()).toString(), rentInfo);
            while (!sent && attempt < 10) {
                try {
                    producer.send(producerRecord).get();
                    sent = true;
                    System.out.println("Successfully sent: " + producerRecord);
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Retrying send due to failure: " + e.getMessage());
                    attempt++;
                }
            }
            producer.commitTransaction();
        } catch (ProducerFencedException pfe) {
            producer.close();
        } catch (KafkaException ke) {
            producer.abortTransaction();
        }
    }

    /**
     * Converts an ObjectId into a UUID. The method extracts and manipulates
     * bytes from the ObjectId to conform to the UUID specification. The generated
     * UUID is of version 4 and IETF variant.
     *
     * @param objectId The ObjectId to be converted into a UUID. The ObjectId
     *                 typically represents a unique identifier from a database
     *                 or other system.
     * @return The UUID generated from the given ObjectId.
     */
    public static UUID toUUID(ObjectId objectId) {
        byte[] objectidBytes = objectId.toByteArray();
        byte[] uuidBytes = new byte[16];

        System.arraycopy(objectidBytes, 0, uuidBytes, 0, 6);
        System.arraycopy(objectidBytes, 6, uuidBytes, 10, 6);

        uuidBytes[6]  &= 0x0f;  /* clear version        */
        uuidBytes[6]  |= 0x40;  /* set to version 4     */
        uuidBytes[8]  &= 0x3f;  /* clear variant        */
        uuidBytes[8]  |= 0x80;  /* set to IETF variant  */

        ByteBuffer buffer = ByteBuffer.wrap(uuidBytes);

        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
