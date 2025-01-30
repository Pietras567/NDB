package Consumer;


import redis.clients.jedis.*;
import org.bson.Document;
import java.io.IOException;
import java.util.Properties;

public class RedisManagerConsumer {
    private static Jedis pool;
    private static final Properties property = new Properties();
    private static final String confFile = "appConsumer.config";

    private void readConfig() {
        try (var fis = getClass().getClassLoader().getResourceAsStream(confFile)) {
            property.load(fis);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void initConnection() {
        this.readConfig();
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder().build();
        pool = new Jedis(new HostAndPort(property.getProperty("app.address"),
                Integer.parseInt(property.getProperty("app.port"))), clientConfig);
    }

    public RedisManagerConsumer() {
        this.initConnection();
        this.testConnection();
    }

    public void testConnection() {
        String res1 = pool.set("bike:2", "cosus");
        System.out.println(res1); // OK

        String res2 = pool.get("bike:2");
        System.out.println(res2); // Deimos
    }

    public Document getDocument(String key) {
        String response = pool.get(key);
        return response != null ? Document.parse(response) : null;
    }

    public void setDocument(String key, Document document, int TTL) {
        if (TTL == 0) {
            pool.set(key, document.toJson());
            return;
        }
        pool.setex(key, TTL, document.toJson());
    }

    public void removeDocument(String key) {
        pool.del(key);
    }

    public long changeTTL(String key, int TTL) {
        return pool.expire(key, TTL);
    }
}
