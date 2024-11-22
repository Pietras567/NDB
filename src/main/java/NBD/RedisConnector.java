package NBD;

import redis.clients.jedis.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class RedisConnector {
    private static JedisPooled pool;
    private static final Properties property = new Properties();
    private static final String confFile = "app.config";

    private void readConfig() {
        try (FileInputStream fis = new FileInputStream(confFile)) {
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

    public RedisConnector() {
        this.initConnection();
    }

    public void testConnection() {
        String res1 = pool.set("bike:2", "cosus");
        System.out.println(res1); // OK

        String res2 = pool.get("bike:2");
        System.out.println(res2); // Deimos
    }
}
