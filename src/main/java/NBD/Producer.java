package NBD;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Producer {
    private static final String[] RENTAL_CENTERS = {"CarRental", "JadymyRental", "ZygzakMcQueen"};
    private DatabaseApi databaseApi;

    public Producer(DatabaseApi databaseApi) {
        this.databaseApi = databaseApi;
        createTopic();
    }

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
        String vehicleName = databaseApi.getEntity(Vehicle.class, "vehicles",rent.getVehicle_id()).getName();
        LocalDateTime startTime = rent.getStartDate();
        LocalDateTime endTime = rent.getEndDate();

        int rentalId = databaseApi.getEntity(Vehicle.class, "vehicles",rent.getVehicle_id()).getRentalId();
        String rentalCenter = RENTAL_CENTERS[rentalId];

        String rentalTime = java.time.LocalDateTime.now().toString();
        String rentInfo = String.format("{\"rentID\": %s, \"rental_center\": \"%s\", \"clientName\": \"%s\", \"vehicleName\": \"%s\", \"rental_time\": \"%s\"}",
                rentID, rentalCenter, clientName, vehicleName, startTime);

        producer.initTransactions();
        try {
            producer.beginTransaction();
            for (int i = 0; i < 10; i++) {
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>("rents", rentInfo);
                // send data - asynchronous
                producer.send(producerRecord);
            }
            producer.commitTransaction();
        } catch (ProducerFencedException pfe) {
            producer.close();
        } catch (KafkaException ke) {
            producer.abortTransaction();
        }


//        // flush data - synchronous
//        producer.flush();
//
//        // flush and close producer
//        producer.close();
    }
}
