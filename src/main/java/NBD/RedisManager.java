package NBD;

import redis.clients.jedis.*;

//import javax.swing.text.Document;
import org.bson.Document;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Manages interactions with a Redis database using a pooled connection.
 * This class is responsible for establishing and maintaining the Redis connection,
 * performing basic CRUD operations, and handling documents with optional time-to-live (TTL) settings.
 */
public class RedisManager {
    private static JedisPooled pool;
    private static final Properties property = new Properties();
    private static final String confFile = "app.config";

    /**
     * Reads configuration data from the specified configuration file.
     * This method attempts to load application properties from a file named in the `confFile` variable
     * located in the classpath using a {@link Properties} instance.
     *
     * If an error occurs while accessing or reading the configuration file, the exception stack trace will be printed.
     * It is expected that the configuration file contains necessary key-value pairs required for application operation.
     */
    private void readConfig() {
        try (var fis = getClass().getClassLoader().getResourceAsStream(confFile)) {
            property.load(fis);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Initializes a pooled connection to the Redis server using configuration settings.
     *
     * This method reads the Redis server connection properties such as address and port
     * by invoking the {@link #readConfig()} method, which loads the required settings
     * from the application's configuration file. Once the properties are loaded,
     * the method creates a new instance of the {@link JedisPooled} client with the specified
     * host, port, and default client configuration.
     *
     * This pooled connection is stored in the static `pool` variable, allowing it to be
     * reused across all interactions with the Redis database.
     *
     * Prerequisites:
     * - The configuration file must include the properties `app.address` and `app.port`.
     * - The Redis server must be accessible with the provided address and port values.
     *
     * Errors or exceptions during configuration loading or connection initialization
     * are not explicitly handled outside of those captured by {@link #readConfig()}.
     */
    public void initConnection() {
        this.readConfig();
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder().build();
        pool = new JedisPooled(new HostAndPort(property.getProperty("app.address"),
                Integer.parseInt(property.getProperty("app.port"))), clientConfig);
    }

    /**
     * Constructor for the RedisManager class.
     *
     * This constructor initializes a pooled connection to the Redis server by invoking
     * the {@link #initConnection()} method. The connection is configured using
     * application settings loaded from a configuration file. This allows the RedisManager
     * instance to provide an interface for managing Redis data operations such as
     * storing, retrieving, and deleting documents using the Redis database.
     *
     * The initialized connection remains active as long as the RedisManager instance
     * exists, supporting efficient, pooled access to the Redis server.
     *
     * Prerequisites:
     * - The configuration file must be properly configured to include Redis server
     *   properties, such as address and port.
     * - A Redis server must be running and accessible with the specified settings.
     *
     * Errors or exceptions during connection initialization are handled internally.
     */
    public RedisManager() {
        this.initConnection();
    }

    public void testConnection() {
        String res1 = pool.set("bike:2", "cosus");
        System.out.println(res1); // OK

        String res2 = pool.get("bike:2");
        System.out.println(res2); // Deimos
    }

    /**
     * Retrieves a document from the Redis database based on the provided key.
     * The method fetches the value associated with the given key from the Redis pool
     * and parses it into a {@link Document} object.
     *
     * @param key the unique identifier used to retrieve the document from the Redis database
     * @return a {@link Document} object if the key exists in Redis and is successfully parsed,
     *         or null if the key does not exist or the value is null
     */
    public Document getDocument(String key) {
        String response = pool.get(key);
        return response != null ? Document.parse(response) : null;
    }

    /**
     * Stores a document in the Redis database with an optional expiration time.
     * If the TTL (Time To Live) is set to 0, the document is stored indefinitely.
     *
     * @param key The unique identifier for the document to be stored in Redis.
     * @param document The document to be stored, represented as a MongoDB {@link Document} object.
     * @param TTL The time-to-live for the document in seconds. A value of 0 indicates no expiration.
     */
    public void setDocument(String key, Document document, int TTL) {
        pool.setex(key, TTL, document.toJson());
    }

    /**
     * Removes a document from the Redis database using the specified key.
     * This method deletes the value associated with the given key from the Redis server.
     *
     * @param key the unique identifier of the document to be removed from the Redis database
     */
    public void removeDocument(String key) {
        pool.del(key);
    }

    /**
     * Changes the Time-To-Live (TTL) for the specified key in the Redis database.
     * This method updates the expiration time of an existing key-value pair, allowing
     * the key to persist for the specified TTL in seconds. If the key does not exist
     * or the TTL is invalid, the operation may fail or return an error.
     *
     * @param key The unique identifier of the key whose TTL is being updated.
     * @param TTL The new time-to-live for the key, in seconds. A value of 0 disables expiration.
     * @return The remaining time-to-live for the key after the update in seconds, or 0 if the key does not exist.
     */
    public long changeTTL(String key, int TTL) {
        return pool.expire(key, TTL);
    }
}
