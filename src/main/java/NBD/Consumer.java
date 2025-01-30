package NBD;

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

public class Consumer {

    private final RedisManager redisManager = new RedisManager();
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
