package eu.kyngas.grapes.music.verticle;

import eu.kyngas.grapes.music.util.AudioUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import javax.sound.sampled.SourceDataLine;
import lombok.extern.slf4j.Slf4j;
import static eu.kyngas.grapes.common.util.Networks.*;
import static eu.kyngas.grapes.music.Launcher.DEFAULT_HTTP_PORT;
import static eu.kyngas.grapes.music.util.AudioUtil.MIXER_INDEX;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public class MusicClientVerticle extends AbstractVerticle {
  private static final boolean DEFAULT_HTTP_SSL = false;

  private SourceDataLine sourceDataLine;
  private HttpClient client;

  @Override
  public void start(Future<Void> fut) throws Exception {
    sourceDataLine = AudioUtil.startAudioPlayback(config().getInteger(MIXER_INDEX));
    client = vertx.createHttpClient(new HttpClientOptions()
                                        .setTrustAll(true)
                                        .setSsl(config().getBoolean(HTTP_SSL, DEFAULT_HTTP_SSL))
                                        .setDefaultPort(config().getInteger(HTTP_PORT, DEFAULT_HTTP_PORT))
                                        .setDefaultHost(config().getString(HTTP_HOST, DEFAULT_HOST)));
    connect(sourceDataLine);
  }

  private void connect(SourceDataLine sourceDataLine) {
    client.websocket("/ws", socket -> {
      log.info("Connected to server");
      // TODO: 27.01.2018 flush sourceDataLine on trigger or on packet loss
      socket.handler(buffer -> {
        byte[] bytes = buffer.getBytes();
        sourceDataLine.write(bytes, 0, bytes.length);
      });
      socket.closeHandler(v -> {
        log.info("Server closed -- attempting reconnect in 500ms");
        vertx.setTimer(500L, t -> connect(sourceDataLine));
      });
      socket.exceptionHandler(thr -> {
        log.error("Caught exception -- attempting to reconnect in 500ms", thr);
        vertx.setTimer(500L, t -> connect(sourceDataLine));
      });
    });
  }

  @Override
  public void stop() {
    sourceDataLine.close();
    client.close();
  }
}
