package eu.kyngas.grapes.music;

import eu.kyngas.grapes.common.util.Eq;
import eu.kyngas.grapes.common.util.LogUtil;
import eu.kyngas.grapes.common.util.ParseUtil;
import eu.kyngas.grapes.music.verticle.MusicClientVerticle;
import eu.kyngas.grapes.music.verticle.MusicVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import static eu.kyngas.grapes.music.util.AudioUtil.MIXER_INDEX;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public class Launcher {

  public static void main(String[] args) {
    LogUtil.setLoggingToSLF4J();
    JsonObject config = new JsonObject().put(MIXER_INDEX, getMixerIndex(args));
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(isRunningAsServer(args)
            ? new MusicVerticle()
            : new MusicClientVerticle(),
        new DeploymentOptions().setConfig(config),
        handleVerticleStarted(vertx));
  }

  private static Handler<AsyncResult<String>> handleVerticleStarted(Vertx vertx) {
    return ar -> {
      if (ar.failed()) {
        log.error("Failed to start Music Module", ar.cause());
        vertx.close();
        return;
      }
      log.info("Music module started");
    };
  }

  private static int getMixerIndex(String[] args) {
    int index = args.length == 1 || args.length == 2 ? ParseUtil.toInteger(args[args.length - 1]) : -1;
    return index == -1 ? 0 : index;
  }

  private static boolean isRunningAsServer(String[] args) {
    return args.length == 0 || args.length == 1 && Eq.eq(args[0], "c", "client");
  }
}
