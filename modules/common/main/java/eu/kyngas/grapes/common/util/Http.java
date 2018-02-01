package eu.kyngas.grapes.common.util;

import eu.kyngas.grapes.common.router.RedirectAction;
import io.vertx.core.http.HttpClientOptions;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class Http {

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
}
