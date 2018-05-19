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

package eu.kyngas.grapes.gateway;

import eu.kyngas.grapes.common.router.AbstractMainRouter;
import eu.kyngas.grapes.common.router.Status;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.Logs;
import eu.kyngas.grapes.common.util.S;
import eu.kyngas.grapes.gateway.config.GatewayConfig;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.VirtualHostHandler;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class MainRouter extends AbstractMainRouter {
  private final HttpClient client;

  public MainRouter() {
    this.client = Ctx.vertx().createHttpClient();
  }

  @Override
  protected void init() {
    get("/kill").handler(ctx -> {
      Ctx.vertx().close();
      Status.ok(ctx);
    });
    GatewayConfig config = Ctx.config().mapTo(GatewayConfig.class);
    config.getVirtualHosts().forEach(host -> {
      Logs.info("Adding virtual host proxy for {} using domains {} routing to {}:{}",
                host.getName(), host.getDomains(), host.getHost(), host.getPort());
      host.getDomains().forEach(domain -> addRoute(route().handler(VirtualHostHandler.create(domain,
                                                                                             handleRequest(host)))));
    });
  }

  private Handler<RoutingContext> handleRequest(GatewayConfig.VirtualHost host) {
    return ctx -> {
      HttpServerRequest req = ctx.request();
      Logs.info("Proxying request {}", S.toString(req));
      HttpClientRequest cReq = client.request(req.method(), host.getPort(), host.getHost(), req.uri(), cRes -> {
        HttpServerResponse res = req.response();
        Logs.info("Proxying response {}", S.toString(res));
        res.setChunked(true).setStatusCode(cRes.statusCode()).headers().setAll(cRes.headers());
        cRes.handler(res::write);
        cRes.endHandler(v -> res.end());
      });
      cReq.setChunked(true).headers().setAll(req.headers());
      cReq.write(ctx.getBody());
      if (req.isEnded()) {
        cReq.end();
        return;
      }
      req.endHandler(v -> cReq.end());
    };
  }
}
