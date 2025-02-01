package Consumer;


import redis.clients.jedis.*;
import org.bson.Document;
import java.io.IOException;
import java.util.Properties;

/**
 * The RedisManagerConsumer class provides functionality to interact with a Redis database.
 * It is responsible for establishing a connection to the Redis server, managing configuration,
 * and performing common operations such as setting, retrieving, and deleting documents.
 */
public class RedisManagerConsumer {
    private static Jedis pool;
    private static final Properties property = new Properties();
    private static final String confFile = "appConsumer.config";

    /**
     * Reads the configuration properties from a configuration file.
     * This method uses the class loader to locate and read the specified configuration file
     * and loads the properties into an internal `Properties` object.
     * If an error occurs while reading the file, the stack trace of the exception is printed.
     *
     * The configuration file is expected to be located in the application classpath.
     */
    private void readConfig() {
        try (var fis = getClass().getClassLoader().getResourceAsStream(confFile)) {
            property.load(fis);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Initializes a connection to the Redis server using the configurations provided
     * in the application configuration file. This method first reads the configuration
     * file through a call to the internal `readConfig` method, then constructs a
     * Jedis instance using the specified host and port details from the configuration.
     * The connection is stored in a static Jedis object to be utilized by other methods
     * in the class.
     *
     * Dependencies:
     * - The configuration file must provide valid Redis server details, including
     *   "app.address" and "app.port".
     *
     * Preconditions:
     * - The `readConfig` method should successfully load the configuration properties, and
     *   the necessary properties must be defined in the configuration file.
     *
     * Postconditions:
     * - A new Jedis connection is established and stored in the static `pool` variable.
     *
     * Potential Exceptions:
     * - If the configuration file is missing or contains invalid values for server
     *   address and port, connection initialization will fail at runtime.
     */
    public void initConnection() {
        this.readConfig();
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder().build();
        pool = new Jedis(new HostAndPort(property.getProperty("app.address"),
                Integer.parseInt(property.getProperty("app.port"))), clientConfig);
    }

    /**
     * Default constructor for the RedisManagerConsumer class.
     * This constructor is responsible for initializing a connection
     * to the Redis server as specified in the application's configuration file.
     * It internally calls the `initConnection` method to establish the connection.
     *
     * Preconditions:
     * - The application configuration file must be properly set up and available
     *   in the classpath for the `readConfig` method to read the necessary properties.
     *
     * Postconditions:
     * - A connection to the Redis server is established, and the necessary internal
     *   configurations are completed to allow further interaction with the Redis database.
     *
     * Potential Exceptions:
     * - If the configuration file is missing, incorrectly formatted, or has invalid
     *   Redis server details, the connection initialization will not succeed.
     */
    public RedisManagerConsumer() {
        this.initConnection();
    }

    public void testConnection() {
        String res1 = pool.set("bike:2", "cosus");
        System.out.println(res1); // OK

        String res2 = pool.get("bike:2");
        System.out.println(res2); // Deimos
    }

    /**
     * Retrieves a document from the Redis data store associated with the specified key.
     * This method queries the Redis server for the value of the given key, parses
     * the result into a Document object, and returns it. If the key does not exist
     * or if the retrieved value is null, the method returns null.
     *
     * @param key the key used to retrieve the document from the Redis data store
     * @return the document associated with the specified key, or null if no document is found
     */
    public Document getDocument(String key) {
        String response = pool.get(key);
        return response != null ? Document.parse(response) : null;
    }

    /**
     * Sets a document in the Redis data store with the specified key and TTL (time-to-live).
     * If TTL is set to 0, the document is stored persistently without expiry.
     * Otherwise, the document is stored with the specified expiration time.
     *
     * @param key the key under which the document will be stored in the Redis data store
     * @param document the document to be stored
     * @param TTL the time-to-live for the document in seconds; if 0, the document does not expire
     */
    public void setDocument(String key, Document document, int TTL) {
        if (TTL == 0) {
            pool.set(key, document.toJson());
            return;
        }
        pool.setex(key, TTL, document.toJson());
    }

    /**
     * Removes a document from the Redis data store associated with the specified key.
     * This method deletes the key-value pair from the Redis server if it exists.
     *
     * @param key the key of the document to be removed from the Redis data store
     */
    public void removeDocument(String key) {
        pool.del(key);
    }

    /**
     * Modifies the Time-To-Live (TTL) value for a specified key in the Redis data store.
     * If the key exists, the TTL value is updated to the specified duration.
     *
     * @param key the key for which the TTL should be updated
     * @param TTL the new TTL value in seconds
     * @return the remaining TTL for the key after the update, or -1 if the key does not exist or does not have an associated TTL
     */
    public long changeTTL(String key, int TTL) {
        return pool.expire(key, TTL);
    }
}
