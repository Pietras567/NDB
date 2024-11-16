package NBD;

import redis.clients.jedis.*;

public class RedisConnector {
    private static JedisPooled pool;

    public void initConnection() {
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder().build();
        pool = new JedisPooled(new HostAndPort("localhost", 6379), clientConfig);
    }

    public RedisConnector() {
        this.initConnection();
    }

    public void testConnection() {
        String res1 = pool.set("bike:1", "Deimos");
        System.out.println(res1); // OK

        String res2 = pool.get("bike:1");
        System.out.println(res2); // Deimos
    }
}
