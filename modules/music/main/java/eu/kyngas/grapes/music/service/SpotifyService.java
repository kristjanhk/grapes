package eu.kyngas.grapes.music.service;

import eu.kyngas.grapes.common.util.Ctx;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@VertxGen
@ProxyGen
public interface SpotifyService {
  String ADDRESS = "music.spotify";

  static SpotifyService create(JsonObject config) {
    return new SpotifyServiceImpl(config);
  }

  static SpotifyService createProxy() {
    return new SpotifyServiceVertxEBProxy(Ctx.vertx(), ADDRESS);
  }

  @Fluent
  SpotifyService getTestData(Handler<AsyncResult<JsonObject>> handler);
}
