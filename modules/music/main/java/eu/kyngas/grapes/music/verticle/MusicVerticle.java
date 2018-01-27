package eu.kyngas.grapes.music.verticle;

import eu.kyngas.grapes.common.util.C;
import eu.kyngas.grapes.music.util.AsyncInputStream;
import eu.kyngas.grapes.music.util.AudioUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketBase;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import lombok.extern.slf4j.Slf4j;
import static eu.kyngas.grapes.common.util.Config.isRunningFromJar;
import static eu.kyngas.grapes.common.util.Network.DEFAULT_HTTP_HOST;
import static eu.kyngas.grapes.common.util.Network.HTTP_HOST;
import static eu.kyngas.grapes.common.util.Network.HTTP_PORT;
import static eu.kyngas.grapes.music.Launcher.DEFAULT_HTTP_PORT;
import static eu.kyngas.grapes.music.util.AudioUtil.MIXER_INDEX;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public class MusicVerticle extends AbstractVerticle {
  private static final int DEFAULT_QUEUE_SIZE = 8192;
  private static final Path RESOURCES = Paths.get("src/main/resources");
  private static final String STATIC_PATH = "/static/*";
  private static final String STATIC_FOLDER = "static";

  private final AtomicBoolean audioRecordingThreadRunning = new AtomicBoolean(true);
  private final ConcurrentHashMap<String, ServerWebSocket> sockets = new ConcurrentHashMap<>();
  private final AtomicReference<AsyncInputStream> lineRef = new AtomicReference<>();

  private int mixerIndex;
  private HttpServer server;

  @Override
  public void start(Future<Void> future) {
    mixerIndex = config().getInteger(MIXER_INDEX);
    Router router = Router.router(vertx);

    String staticFilesPath = isRunningFromJar() ? STATIC_FOLDER : RESOURCES.resolve(STATIC_FOLDER).toString();
    router.get(STATIC_PATH).handler(StaticHandler.create(staticFilesPath)
                                                 .setCachingEnabled(false)
                                                 .setIncludeHidden(false)
                                                 .setDirectoryListing(true));

    router.get("/ws").handler(ctx -> startAudioTransfer(ctx.request().upgrade()));
    server = vertx.createHttpServer()
                  .requestHandler(router::accept)
                  .listen(config().getInteger(HTTP_PORT, DEFAULT_HTTP_PORT),
                          config().getString(HTTP_HOST, DEFAULT_HTTP_HOST),
                          handleServerStarted(future));
  }

  private Handler<AsyncResult<HttpServer>> handleServerStarted(Future<Void> future) {
    return ar -> {
      if (ar.failed()) {
        log.error("Failed to start http server", ar.cause());
        future.fail(ar.cause());
        return;
      }
      log.info("Http server started");
      future.complete();
    };
  }

  private void startAudioTransfer(ServerWebSocket socket) {
    socket.closeHandler(v -> handleClientDisconnect(socket));

    sockets.put(socket.textHandlerID(), socket);
    socket.setWriteQueueMaxSize(DEFAULT_QUEUE_SIZE);
    if (sockets.size() > 1) {
      log.info("Client {} connected -- reusing recording thread", socket.textHandlerID());
      return;
    }
    log.info("Client {} connected -- starting new recording thread", socket.textHandlerID());

    TargetDataLine line;
    try {
      line = AudioUtil.startAudioRecording(mixerIndex);
    } catch (LineUnavailableException e) {
      log.error("Failed to open TargetDataLine -- rejecting client", e);
      socket.reject();
      return;
    }
    AsyncInputStream in = new AsyncInputStream(new AudioInputStream(line));
    lineRef.set(in);
    in.endHandler(v -> C.check(sockets.isEmpty(), () -> log.info("Audio input closed"), () -> {
      log.error("Audio input closed -- disconnecting clients");
      sockets.values().forEach(WebSocketBase::close);
    }));
    in.exceptionHandler(thr -> log.error("Exception in audio input", thr));
    // TODO: 27.01.2018 try to clear queue to sync?
    in.handler(buffer -> sockets.values().forEach(ws -> C.check(!ws.writeQueueFull(),
                                                                () -> ws.write(buffer),
                                                                () -> log.debug("Client {}: write queue full",
                                                                                socket.textHandlerID()))));
  }

  private void handleClientDisconnect(ServerWebSocket socket) {
    sockets.remove(socket.textHandlerID());
    log.info("Client {} disconnected", socket.textHandlerID());
    if (sockets.isEmpty()) {
      log.info("All clients disconnected -- closing TargetDataLine recording");
      audioRecordingThreadRunning.set(false);
      lineRef.get().close();
    }
  }

  @Override
  public void stop(Future<Void> fut) {
    audioRecordingThreadRunning.set(false);
    lineRef.get().close();
    server.close(ar -> C.check(ar.succeeded(), fut::complete, () -> {
      log.error("Failed to properly close http server", ar.cause());
      fut.fail(ar.cause());
    }));
  }
}
