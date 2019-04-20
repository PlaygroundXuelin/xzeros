package xzeros;

import redis.embedded.RedisServer;

public class RedisTestUtils {
  public static RedisServer startServer(int port) {
    RedisServer redisServer = new RedisServer(port);
    redisServer.start();
    return redisServer;
  }

  public static void stopServer(RedisServer server) {
    server.stop();
  }

  public static void main(String[] args) {
    try {
      startServer(7634);
      int i = 0;
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
