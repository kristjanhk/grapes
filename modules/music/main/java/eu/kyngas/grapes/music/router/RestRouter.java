/*
 * Copyright (C) 2018 Kristjan Hendrik Küngas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.kyngas.grapes.music.router;

import eu.kyngas.grapes.common.router.Status;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.Strings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RouterImpl;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public abstract class RestRouter extends RouterImpl {

  public RestRouter() {
    super(Ctx.vertx());
  }

  public void subRouterTo(Router parent, String path) {
    parent.mountSubRouter(path, this);
    addRoutes();
    log.debug("{} registered routes under {}: {}",
              getClass().getSimpleName(), path, Strings.join(getRoutes(), Route::getPath));
  }

  protected abstract void addRoutes();

  protected <T> Handler<AsyncResult<T>> handler(RoutingContext ctx, Consumer<T> consumer) {
    return ar -> {
      if (ar.failed()) {
        log.error("RestRouter handler failure", ar.cause());
        Status.internalError(ctx, ar.cause());
        return;
      }
      if (consumer == null) {
        log.error("RestRouter passed null handler");
        Status.notFound(ctx);
        return;
      }
      log.info("User: {}, path: {}, data: {}", "dummy", ctx.request().path(), ar.result());
      consumer.accept(ar.result());
    };
  }

  protected Handler<AsyncResult<JsonObject>> jsonResponse(RoutingContext ctx) {
    return handler(ctx, json -> ctx.response().end(json.encodePrettily()));
  }
}
