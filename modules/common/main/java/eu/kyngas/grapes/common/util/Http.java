package eu.kyngas.grapes.common.util;

import eu.kyngas.grapes.common.router.RedirectAction;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class Http {
  private static final String APPLICATION_JSON = "application/json";
  private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

  public static final String HOST = "host";
  public static final String PORT = "port";
  public static final String SSL = "ssl";

  @RequiredArgsConstructor(staticName = "of")
  public static class Query {
    private final Map<String, Object> params = new HashMap<>();
    private final String url;
    private String host;

    public Query param(String key, Object value) {
      params.put(key, value);
      return this;
    }

    public Query host(String host) {
      this.host = host;
      return this;
    }

    // TODO: 30.01.2018 params should be escaped?
    public String create() {
      return host != null ? host : "" + (url.charAt(0) == '/' ? url : '/' + url) + params.entrySet().stream()
          .map(e -> e.getKey() + "=" + e.getValue())
          .collect(Collectors.joining("&", "?", ""));
    }

    public String toFullUrl(int port, String host) {
      return String.format("http%s://", port == 443 ? "s" : "") + host + create();
    }

    public RedirectAction toRedirectAction(int port, String host) {
      return new RedirectAction(toFullUrl(port, host));
    }

    public RedirectAction toRedirectAction(HttpClientOptions options) {
      return new RedirectAction(toFullUrl(options.getDefaultPort(), options.getDefaultHost()));
    }
  }

  public static void logEnd(HttpClientRequest request) {
    Logs.info("Request: {}", S.toString(request));
    request.end();
  }

  public static HttpClient createClient(JsonObject config) {
    Objects.requireNonNull(config.getString(HOST));
    HttpClientOptions options = new HttpClientOptions()
        .setDefaultHost(config.getString(HOST))
        .setDefaultPort(config.getInteger(PORT, 443))
        .setSsl(config.getBoolean(SSL, true));
    return Ctx.vertx().createHttpClient(options);
  }

  public static String createBasicAuthHeader(String username, String password) {
    return "Basic " + S.base64("%s:%s", username, password);
  }

  public static HttpClientRequest basicAuth(HttpClientRequest request, String username, String password) {
    return addContentType(request, APPLICATION_X_WWW_FORM_URLENCODED)
        .putHeader(AUTHORIZATION, createBasicAuthHeader(username, password));
  }

  public static HttpClientRequest addContentType(HttpClientRequest request, String type) {
    String oldType = request.headers().get(CONTENT_TYPE);
    return oldType == null
        ? request.putHeader(CONTENT_TYPE, type)
        : request.putHeader(CONTENT_TYPE, S.format("%s; %s", oldType, type));
  }

  public static HttpClientRequest sendJson(HttpClientRequest request, JsonObject json) {
    return addContentType(request, APPLICATION_JSON).setChunked(true).write(json.toBuffer());
  }
}
