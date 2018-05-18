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

import eu.kyngas.grapes.common.entity.JsonObj;
import eu.kyngas.grapes.common.util.Config;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.H;
import eu.kyngas.grapes.common.util.Logs;
import eu.kyngas.grapes.common.util.Networks;
import eu.kyngas.grapes.gateway.acme.AcmeService;
import eu.kyngas.grapes.gateway.http.HttpService;
import eu.kyngas.grapes.gateway.http.HttpServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class GatewayVerticle extends AbstractVerticle {
  private static final String MODULE_NAME = "Gateway";

  private HttpService httpService;
  private AcmeService acmeService;

  public static void main(String[] args) {
    Logs.init(MODULE_NAME);
    JsonObj config = Config.getGlobal()
        .deepMergeIn(Config.getJson(MODULE_NAME))
        .deepMergeIn(Config.getArgs(args));
    Ctx.create(new VertxOptions().setAddressResolverOptions(Networks.getDnsResolverOptions(config)),
               vertx -> vertx.deployVerticle(new GatewayVerticle(),
                                             new DeploymentOptions().setConfig(config),
                                             ar -> H.handleVerticleStarted(vertx, MODULE_NAME).handle(ar)));
  }

  @Override
  public void start(Future<Void> future) {
    acmeService = AcmeService.create();
    HttpServiceImpl.createHttpService(new MainRouter(), config()).compose(service -> {
      httpService = service;
      return acmeService.renewCertificate("kyngas.eu");
    }).setHandler(future);
  }

  @Override
  public void stop() {
    httpService.close();
  }
}
