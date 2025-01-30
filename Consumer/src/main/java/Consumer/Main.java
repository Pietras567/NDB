package Consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        System.out.print("Tryb pracy konsumenta Apache Kafka");
        Consumer consumer = new Consumer();
        List<KafkaConsumer<UUID, String>> consumers = consumer.createConsumerGroup();
        consumer.consume(consumers.getFirst());
    }
}