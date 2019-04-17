package xzeros;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Jwt {
  private static final String JWT_SECRET = UUID.randomUUID().toString();

  public static String createTokenForSubject(String subject) {
    Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.HOUR, 1);
    String token = JWT.create()
        .withIssuer("xzero")
        .withSubject(subject)
        .withExpiresAt(cal.getTime())
        .sign(algorithm);
    return token;
  }

  public static String decodeTokenSubject(String token) {
    try {
      DecodedJWT jwt = JWT.decode(token);
      Date now = new Date();
      if (now.after(jwt.getExpiresAt())) {
        return null;
      }
      return jwt.getSubject();
    }
    catch (Exception ex) {
      return null;
    }
  }

  public static void main(String[] args) {
    String token = createTokenForSubject("xlw");
    System.out.printf("Token=%s\n", token);
    String subject = decodeTokenSubject(token);
    System.out.printf("Subject=%s\n", subject);
  }
}
