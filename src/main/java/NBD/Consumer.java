package NBD;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.UUIDDeserializer;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class Consumer {

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
}
