package NBD;

import redis.clients.jedis.*;

//import javax.swing.text.Document;
import org.bson.Document;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class RedisManager {
    private static JedisPooled pool;
    private static final Properties property = new Properties();
    private static final String confFile = "app.config";

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
        pool = new JedisPooled(new HostAndPort(property.getProperty("app.address"),
                Integer.parseInt(property.getProperty("app.port"))), clientConfig);
    }

    public RedisManager() {
        this.initConnection();
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

    public void setDocument(String key, Document document) {
        pool.set(key, document.toJson());
    }

    public void removeDocument(String key) {
        pool.del(key);
    }
}
