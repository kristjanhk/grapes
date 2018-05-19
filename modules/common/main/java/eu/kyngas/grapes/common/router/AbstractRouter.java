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

package eu.kyngas.grapes.common.router;

import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.Logs;
import eu.kyngas.grapes.common.util.N;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.impl.RouterImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public abstract class AbstractRouter extends RouterImpl {
  private final Map<String, Route> routes = new HashMap<>();

  public AbstractRouter() {
    super(Ctx.vertx());
    route().handler(BodyHandler.create());
  }

  public void addRoute(String url, String response) { // TODO: 2.05.2018 different responses, http codes...
    if (hasRoute(url)) {
      Logs.warn("Router {} already contains route with url {}.", getClass().getSimpleName(), url);
    }
    routes.putIfAbsent(url, route(url).handler(ctx -> Status.ok(ctx, response)));
  }

  protected void addRoute(Route route) {
    routes.putIfAbsent(route.getPath(), route);
  }

  public void removeRoute(String url) {
    N.safe(routes.remove(url), Route::remove);
  }

  protected boolean hasRoute(String url) {
    return url != null && routes.containsKey(url);
  }

  protected <T> Handler<AsyncResult<T>> handler(RoutingContext ctx, Consumer<T> consumer) {
    return ar -> {
      if (ar.failed()) {
        Logs.error("RestRouter handler failure", ar.cause());
        Status.internalError(ctx, ar.cause());
        return;
      }
      if (consumer == null) {
        Logs.error("RestRouter passed null handler");
        Status.notFound(ctx);
        return;
      }
      Logs.info(4, "User: {}, path: {}, response: {}", "dummy", ctx.request().path(), ar.result());
      consumer.accept(ar.result());
    };
  }

  protected Handler<AsyncResult<Void>> voidResponse(RoutingContext ctx) {
    return handler(ctx, v -> Status.ok(ctx));
  }

  protected Handler<AsyncResult<Boolean>> booleanResponse(RoutingContext ctx) {
    return handler(ctx, bool -> Status.ok(ctx, new JsonObject().put("value", bool)));
  }

  protected Handler<AsyncResult<JsonObject>> jsonResponse(RoutingContext ctx) {
    return handler(ctx, json -> Status.ok(ctx, json));
  }

  protected Handler<RoutingContext> withParam(String name, BiConsumer<RoutingContext, String> ctxParamConsumer) {
    return ctx -> {
      String param = ctx.request().getParam(name);
      if (param == null) {
        Status.badRequest(ctx, new Throwable(String.format("Missing parameter: %s", name)));
        return;
      }
      ctxParamConsumer.accept(ctx, param);
    };
  }
}
