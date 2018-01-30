package eu.kyngas.grapes.common.util;

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
    private final String url;
    private final Map<String, Object> params = new HashMap<>();

    public Query param(String key, Object value) {
      params.put(key, value);
      return this;
    }

    // TODO: 30.01.2018 params should be escaped?
    public String create() {
      return url.charAt(0) == '/' ? url : '/' + url + params.entrySet().stream()
          .map(e -> e.getKey() + "=" + e.getValue())
          .collect(Collectors.joining("&", "?", ""));
    }

    public String createFullUrl(int port, String host) {
      return String.format("http%s://", port == 443 ? "s" : "") + host + create();
    }
  } 
}
