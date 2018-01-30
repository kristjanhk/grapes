package eu.kyngas.grapes.music;

import eu.kyngas.grapes.common.util.C;
import eu.kyngas.grapes.common.util.Config;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.entity.JsonObj;
import eu.kyngas.grapes.common.util.LogUtil;
import eu.kyngas.grapes.music.verticle.MusicClientVerticle;
import eu.kyngas.grapes.music.verticle.MusicVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public class Launcher {
  public static final int DEFAULT_HTTP_PORT = 8085;

  public static void main(String[] args) {
    LogUtil.setLoggingToSLF4J();
    JsonObject arguments = Config.getArgs(args);
    boolean runningAsServer = !arguments.containsKey("client") || !arguments.getBoolean("client", false);

    JsonObj config = runningAsServer ? Config.getConfig() : Config.getConfig("client-config");
    Ctx.createVertx(vertx -> vertx.deployVerticle(runningAsServer ? new MusicVerticle() : new MusicClientVerticle(),
                                                  new DeploymentOptions().setConfig(config.mergeIn(arguments)),
                                                  handleVerticleStarted(vertx)));
  }

  private static Handler<AsyncResult<String>> handleVerticleStarted(Vertx vertx) {
    return ar -> C.check(ar.succeeded(), () -> log.info("Music module started"), () -> {
      log.error("Failed to start Music Module", ar.cause());
      vertx.close();
    });
  }
}
