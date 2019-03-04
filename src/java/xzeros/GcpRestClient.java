package xzeros;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.collect.ImmutableSet;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GcpRestClient {
  private static class DefaultContentTypeInterceptor implements Interceptor {
    private final String contentType;
    public DefaultContentTypeInterceptor(String contentType) {
      this.contentType = contentType;
    }
    public Response intercept(Interceptor.Chain chain)
        throws IOException {

      Request originalRequest = chain.request();
      Request requestWithUserAgent = originalRequest
          .newBuilder()
          .header("Content-Type", contentType)
          .build();

      return chain.proceed(requestWithUserAgent);
    }
  }

  private class AuthInterceptor implements Interceptor {
    public AuthInterceptor() {
    }
    public Response intercept(Interceptor.Chain chain)
        throws IOException {

      Request originalRequest = chain.request();
      Request requestWithUserAgent = originalRequest
          .newBuilder()
          .header("Authorization", "Bearer " + getToken(5, 1000))
          .build();

      return chain.proceed(requestWithUserAgent);
    }
  }

  private String getToken(int maxRetry, int expireThresholdMs) throws IOException {
    AccessToken accessToken = serviceAccountCredentials.getAccessToken();
    if (accessToken == null ||
        accessToken.getExpirationTime().getTime() - System.currentTimeMillis() < expireThresholdMs) {
      int retryCount = 0;
      IOException ex = null;
      while (retryCount < maxRetry) {
        try {
          accessToken = serviceAccountCredentials.refreshAccessToken();
          return accessToken.getTokenValue();
        }
        catch(IOException thisEx) {
          ex = thisEx;
          try {
            Thread.sleep(200);
          }
          catch (InterruptedException ie) {
            throw new RuntimeException(ie);
          }
        }
      }
      throw ex;
    }
    else {
      return accessToken.getTokenValue();
    }
  }

  private final OkHttpClient client;
  private final ServiceAccountCredentials serviceAccountCredentials;

  public GcpRestClient(String credsFile) throws IOException {
    ObjectMapper objMapper = new ObjectMapper();
    objMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT.FAIL_ON_UNKNOWN_PROPERTIES);
    Map<String, Object> credsMap = objMapper.readValue(new String(Files.readAllBytes(Paths.get(credsFile)), "UTF-8"),
        new TypeReference(){});

    Map<String, Object> data = (Map<String, Object>)credsMap.get("data");
    Map<String, Object> attributes = (Map<String, Object>)data.get("attributes");

    String secret = (String)attributes.get("secret");
    Map<String, Object> creds = objMapper.readValue(secret, new TypeReference<Map<String,Object>>(){});

    Set<String> scopes = ImmutableSet.<String>builder()
        .add("https://www.googleapis.com/auth/devstorage.full_control")
        .add("https://www.googleapis.com/auth/cloud-platform.read-only")
        .add("https://www.googleapis.com/auth/compute.readonly")
        .add("https://www.googleapis.com/auth/cloud-platform")
        .add("https://www.googleapis.com/auth/ndev.clouddns.readonly")
        .build();

    serviceAccountCredentials = ServiceAccountCredentials
        .fromStream(new ByteArrayInputStream(secret.getBytes()))
        .toBuilder()
        .setServiceAccountUser(String.valueOf(creds.get("client_email")))
        .setScopes(scopes)
        .build();

    client = new OkHttpClient.Builder()
        .addInterceptor(new AuthInterceptor())
        .addInterceptor(
            new DefaultContentTypeInterceptor("application/json"))
        .build();
  }

  private void curl(String httpMethod, String urlPattern, Map<String, String> vars,
      String requestBodyFormat, byte[] bodyBytes, Callback callback) {
    String url = replaceVars(urlPattern, vars);

    Request.Builder requestBuilder = new Request.Builder().url(url);
    switch (httpMethod.toLowerCase()) {
      case "post":
        RequestBody body = RequestBody.create(requestBodyFormat == null ? null :
            MediaType.parse(requestBodyFormat), bodyBytes);
        requestBuilder.post(body);
        break;
    }

    Request request = requestBuilder.build();
    final Call call = client.newCall(request);

    call.enqueue(callback);
  }

  private static String replaceVars(String urlPattern, Map<String, String> vars) {
    String url = urlPattern;
    if (vars != null) {
      for (Map.Entry<String, String> entry : vars.entrySet()) {
        url = url.replace("{" + entry.getKey() + "}", entry.getValue());
      }
    }
    return url;
  }
}
