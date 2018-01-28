package eu.kyngas.grapes.music.service;

import eu.kyngas.grapes.common.util.Ctx;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@RequiredArgsConstructor
public class SpotifyServiceImpl implements SpotifyService {
  private final JsonObject config;
  private final Vertx vertx = Ctx.vertx();

  @Override
  public SpotifyService getTestData(Handler<AsyncResult<JsonObject>> handler) {
    handler.handle(Future.succeededFuture(new JsonObject().put("data", "value")));
    return this;
  }
}
