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

package eu.kyngas.grapes.mikrotik;

import eu.kyngas.grapes.common.entity.JsonObj;
import eu.kyngas.grapes.common.util.Config;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.H;
import eu.kyngas.grapes.common.util.Logs;
import eu.kyngas.grapes.mikrotik.ping.PingService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class MikrotikVerticle extends AbstractVerticle {
  private PingService pingService;

  public static void main(String[] args) {
    Logs.setLoggingToSLF4J();
    JsonObject arguments = Config.getArgs(args);
    JsonObj config = Config.getGlobal().mergeIn(Config.getConfig("mikrotik"));
    Ctx.create(vertx -> vertx.deployVerticle(new MikrotikVerticle(),
                                             new DeploymentOptions().setConfig(config.mergeIn(arguments)),
                                             ar -> H.handleVerticleStarted(vertx, "Mikrotik").handle(ar)));
  }

  @Override
  public void start() {
    pingService = PingService.create();
    pingService.pingSuccess(ping -> Logs.info("Ping ok: {}", ping));
    pingService.pingFail(ping -> Logs.info("Ping fail: {}", ping));
    pingService.disconnected(ping -> Logs.info("Disconnected: {}", ping));
    pingService.connected(ping -> Logs.info("Connected: {}", ping));
  }

  @Override
  public void stop(Future<Void> fut) {
    pingService.close(v -> fut.complete());
  }
}
