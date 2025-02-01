package Consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * The Consumer class is responsible for the implementation of a Kafka consumer
 * that listens and processes messages from Kafka topics. It provides methods
 * to create a consumer group and consume messages from a specified Kafka topic.
 * The consumed messages are processed and stored in a Redis database via the
 * RedisManagerConsumer.
 */
public class Consumer {
    private final RedisManagerConsumer redisManager = new RedisManagerConsumer();

    /**
     * Creates and initializes a consumer group of Apache Kafka consumers.
     * Each consumer in the group is configured with the necessary properties to consume
     * messages from Kafka topics, including deserialization settings, group ID, bootstrap server
     * addresses, and isolation level. The consumers are subscribed to the target topic and prepared
     * for message consumption.
     *
     * @return a list of KafkaConsumer instances configured to consume messages with a key of type UUID
     *         and a value of type String from the specified Kafka topic.
     */
    public List<KafkaConsumer<UUID, String>> createConsumerGroup() {
        List<KafkaConsumer<UUID, String>> consumers = new ArrayList<>();

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "rents");
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
        consumerProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        KafkaConsumer<UUID, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(List.of("rents"));
        consumers.add(consumer);

        return consumers;
    }

    /**
     * Consumes messages from an Apache Kafka topic and processes them in a continuous loop.
     * Each consumed message is formatted, logged, and stored into a Redis database.
     *
     * @param consumer a KafkaConsumer instance configured to consume messages with a key of type UUID
     *                 and a value of type String from a Kafka topic.
     */
    public void consume(KafkaConsumer<UUID, String> consumer) {
        try {
            MessageFormat formatter = new MessageFormat("Temat {0}, partition {1}, offset {2, number, integer}, klucz {3}, wartosc {4}");
            while (true) {
                Duration duration = Duration.ofMillis(100);
                ConsumerRecords<UUID, String> records = consumer.poll(duration);

                for (ConsumerRecord<UUID, String> record : records) {
                    String result = formatter.format(new Object[]{record.topic(), record.partition(), record.offset(), toObjectId(record.key()), record.value()});
                    System.out.println("Reading: " + result);

                    Document document = new Document();

                    JSONObject jsonObject = new JSONObject(record.value());
                    System.out.println(jsonObject.toString(4));
                    for (Object key : jsonObject.keySet()) {
                        document.append(key.toString(), jsonObject.get(key.toString()));
                    }

                    //document.append("Rent", record.value());
                    redisManager.setDocument("RentFromBroker:"+toObjectId(record.key()).toString().replaceFirst("^0+(?!$)", ""), document, 0);
                }
                consumer.commitSync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts a UUID into an ObjectId, provided the UUID was originally created from an ObjectId.
     *
     * @param uuid the UUID to convert into an ObjectId
     * @return the corresponding ObjectId representation of the given UUID
     * @throws IllegalArgumentException if the provided UUID was not created from an ObjectId
     */
    public static ObjectId toObjectId(UUID uuid) {
        byte[] uuidBytes = new byte[16];
        ((ByteBuffer) ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).position(0))
                .get(uuidBytes);

        if (uuidBytes[6]!=64 || uuidBytes[7]!=0 || uuidBytes[8]!=-128 || uuidBytes[9]!=0)
            throw new IllegalArgumentException(String.format("UUID: %s has not been created from ObjectId", uuid.toString()));

        byte[] objectidBytes = new byte[12];

        System.arraycopy(uuidBytes, 0, objectidBytes, 0, 6);
        System.arraycopy(uuidBytes, 10, objectidBytes, 6, 6);

        return new ObjectId(objectidBytes);
    }
}
