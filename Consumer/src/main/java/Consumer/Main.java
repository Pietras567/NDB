package Consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.util.List;
import java.util.UUID;

/**
 * The Main class serves as the entry point for the application, which demonstrates
 * the consumer workflow of Apache Kafka. It initializes a Kafka consumer group
 * and processes messages from a Kafka topic.
 *
 * This class:
 * - Displays a message regarding the consumer's operation mode.
 * - Creates a consumer group using the Consumer class.
 * - Consumes messages using one of the Kafka consumers created in the group.
 *
 * The KafkaConsumer processes messages with a key of type UUID and a value of type String.
 * Messages consumed from the Kafka topic are further processed and stored in a Redis database.
 */
public class Main {
    public static void main(String[] args) {
        System.out.print("Tryb pracy konsumenta Apache Kafka");
        Consumer consumer = new Consumer();
        List<KafkaConsumer<UUID, String>> consumers = consumer.createConsumerGroup();
        consumer.consume(consumers.getFirst());
    }
}