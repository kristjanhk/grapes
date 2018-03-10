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

package eu.kyngas.grapes.music;

import eu.kyngas.grapes.common.entity.JsonObj;
import eu.kyngas.grapes.common.entity.Verticle;
import eu.kyngas.grapes.common.util.C;
import eu.kyngas.grapes.common.util.Config;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.H;
import eu.kyngas.grapes.common.util.Logs;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import static eu.kyngas.grapes.common.util.Config.isRunningFromJar;
import static eu.kyngas.grapes.common.util.Networks.*;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class MusicVerticle extends Verticle {
  private static final Path RESOURCES = Paths.get("modules/music/main/resources");
  private static final String STATIC_PATH = "/static/*";
  private static final String STATIC_FOLDER = "static";

  private HttpServer server;

  public static void main(String[] args) {
    Logs.setLoggingToSLF4J();
    JsonObj config = Config.getGlobal()
        .deepMergeIn(Config.getConfig("music"))
        .deepMergeIn(Config.getArgs(args));
    Ctx.create(vertx -> vertx.deployVerticle(new MusicVerticle(),
                                             new DeploymentOptions().setConfig(config),
                                             ar -> H.handleVerticleStarted(vertx, "Music").handle(ar)));
  }

  @Override
  public void start(Future<Void> future) {
    Router router = new MainRouter();
    String staticFilesPath = isRunningFromJar() ? STATIC_FOLDER : RESOURCES.resolve(STATIC_FOLDER).toString();
    router.get(STATIC_PATH).handler(StaticHandler.create(staticFilesPath)
                                                 .setCachingEnabled(false)
                                                 .setIncludeHidden(false)
                                                 .setDirectoryListing(true));
    Logs.info("Static files served from {}", "https://kyngas.eu/static/");
    server = vertx.createHttpServer()
                  .requestHandler(router::accept)
                  .listen(config().getInteger(HTTP_PORT, 8085),
                          config().getString(HTTP_HOST, DEFAULT_HOST),
                          handleServerStarted(future));
  }

  private Handler<AsyncResult<HttpServer>> handleServerStarted(Future<Void> future) {
    return ar -> {
      if (ar.failed()) {
        Logs.error("Failed to start http server", ar.cause());
        future.fail(ar.cause());
        return;
      }
      future.complete();
    };
  }

  @Override
  public void stop(Future<Void> fut) {
    server.close(ar -> C.check(ar.succeeded(), fut::complete, () -> {
      Logs.error("Failed to properly close http server", ar.cause());
      fut.fail(ar.cause());
    }));
  }
}
