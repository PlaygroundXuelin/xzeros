package xzeros;

import java.time.Duration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Redis {
  private static String host;
  private static int port;
  private static JedisPool jedisPool;
  public static JedisPool getJedisPool(String host, int port) {
    if (jedisPool != null) {
      if (!host.equals(Redis.host) || port != Redis.port) {
        throw new RuntimeException("A pool exists with different host/port. poolHost=" + Redis.host +
            ", poolPort=" + Redis.port + ", host=" + host + ", port=" + port);
      }
    }
    else {
      jedisPool = new JedisPool(buildPoolConfig(), host, port);
      Redis.host = host;
      Redis.port = port;
    }
    return jedisPool;
  }

  private static JedisPoolConfig buildPoolConfig() {
    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(128);
    poolConfig.setMaxIdle(128);
    poolConfig.setMinIdle(16);
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestOnReturn(true);
    poolConfig.setTestWhileIdle(true);
    poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
    poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
    poolConfig.setNumTestsPerEvictionRun(3);
    poolConfig.setBlockWhenExhausted(true);
    return poolConfig;
  }

  public static Long hsetStr(Jedis jedis, String k, String f, String v)
  {
    return jedis.hset(k, f, v);
  }

  public static Long hdelStr(Jedis jedis, String k, String[] fs)
  {
    return jedis.hdel(k, fs);
  }
}
