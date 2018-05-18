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

package eu.kyngas.grapes.gateway.http;

import eu.kyngas.grapes.common.router.AbstractMainRouter;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.F;
import eu.kyngas.grapes.common.util.N;
import eu.kyngas.grapes.proxy.ProxyServiceImpl;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import static eu.kyngas.grapes.common.util.Networks.HTTP_HOST;
import static eu.kyngas.grapes.common.util.Networks.HTTP_PORT;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class HttpServiceImpl extends ProxyServiceImpl implements HttpService {
  private final JsonObject config;
  private final AbstractMainRouter router;

  private HttpServer server;

  private HttpServiceImpl(AbstractMainRouter router, String address, JsonObject config) {
    super(address, HttpService.class);
    this.config = config;
    this.router = router;
    Objects.requireNonNull(config.getString(HTTP_HOST), "Config: 'http_host' param is undefined.");
    Objects.requireNonNull(config.getInteger(HTTP_PORT), "Config: 'http_port' param is undefined.");
  }

  public static Future<HttpService> createHttpService(AbstractMainRouter router, JsonObject config) {
    return createHttpService(router, ADDRESS, config);
  }

  public static Future<HttpService> createHttpService(AbstractMainRouter router, String address, JsonObject config) {
    HttpServiceImpl httpService = new HttpServiceImpl(router, address, config);
    return httpService.startServer().map(httpService);
  }

  private Future<HttpServer> startServer() {
    return F.future(fut -> server =
        Ctx.vertx().createHttpServer(new HttpServerOptions()) // TODO: 7.05.2018 load cert from config
            .requestHandler(router::accept)
            .listen(config.getInteger(HTTP_PORT),
                    config.getString(HTTP_HOST), fut));
  }

  @Override
  public HttpService addRoute(String url, String response) {
    router.addRoute(url, response);
    return this;
  }

  @Override
  public HttpService removeRoute(String url) {
    router.removeRoute(url);
    return this;
  }

  @Override
  public HttpService reloadCertificate() {
    // TODO: 7.05.2018 load cert from file + restart http server / dynamic reload
    return this;
  }

  @Override
  public void close() {
    super.close();
    router.close();
    N.safe(server, HttpServer::close);
  }
}
