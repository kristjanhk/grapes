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

import eu.kyngas.grapes.common.util.C;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.Logs;
import eu.kyngas.grapes.common.util.Strings;
import io.vertx.core.Handler;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.impl.RouterImpl;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public abstract class AbstractMainRouter extends RouterImpl {
  private final List<RestRouter> restRouters = new ArrayList<>();
  private final List<SockJsRouter> sockJsRouters = new ArrayList<>();

  public AbstractMainRouter() {
    super(Ctx.vertx());
    init();
    initRoutes();
    initSockjsRoutes();
  }

  protected abstract void init();

  protected void initRoutes() {
    // TODO: 1.02.2018 impl common routes -> static handler etc.
    restRouters.forEach(router -> router.subRouterTo(this, router.getPath()));
  }

  protected void initSockjsRoutes() {
    BridgeOptions options = new BridgeOptions();
    sockJsRouters.forEach(router -> {
      List<PermittedOptions> inbound = new ArrayList<>();
      List<PermittedOptions> outbound = new ArrayList<>();
      router.permitRoutes(inbound, outbound);
      options.getInboundPermitteds().addAll(inbound);
      options.getOutboundPermitteds().addAll(outbound);
      C.ifFalse(inbound.isEmpty(), () -> Logs.debug("{} permitted inbound: {}",
                                                    router.getClass().getSimpleName(),
                                                    permittedToString(inbound)));
      C.ifFalse(outbound.isEmpty(), () -> Logs.debug("{} permitted outbound: {}",
                                                     router.getClass().getSimpleName(),
                                                     permittedToString(outbound)));
    });
    route("/eventbus/*").handler(SockJSHandler.create(Ctx.vertx()).bridge(options, interceptor()));
  }

  protected void addRestRouter(RestRouter restRouter) {
    restRouters.add(restRouter);
  }

  protected void addSockJsRouter(SockJsRouter sockJsRouter) {
    sockJsRouters.add(sockJsRouter);
  }

  protected <T extends RestRouter & SockJsRouter> void addCombinedRouter(T combinedRouter) {
    restRouters.add(combinedRouter);
    sockJsRouters.add(combinedRouter);
  }

  private String permittedToString(List<PermittedOptions> routes) {
    return Strings.join(routes, route -> route.getAddress() != null ? route.getAddress() : route.getAddressRegex());
  }

  private Handler<BridgeEvent> interceptor() {
    return event -> {
      boolean completed = sockJsRouters.stream()
          .peek(router -> router.intercept(event))
          .anyMatch(router -> event.isComplete());
      C.ifFalse(completed, () -> event.complete(true));
    };
  }

  public void close() {
    restRouters.forEach(RestRouter::close);
  }
}
