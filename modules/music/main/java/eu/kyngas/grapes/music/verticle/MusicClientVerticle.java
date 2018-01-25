package eu.kyngas.grapes.music.verticle;

import eu.kyngas.grapes.music.util.AudioUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.RequestOptions;
import javax.sound.sampled.SourceDataLine;
import lombok.extern.slf4j.Slf4j;
import static eu.kyngas.grapes.music.util.AudioUtil.MIXER_INDEX;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public class MusicClientVerticle extends AbstractVerticle {
  private SourceDataLine sourceDataLine;
  private HttpClient client;

  @Override
  public void start(Future<Void> fut) throws Exception {
    sourceDataLine = AudioUtil.startAudioPlayback(config().getInteger(MIXER_INDEX));
    client = vertx.createHttpClient(new HttpClientOptions().setTrustAll(true).setSsl(true));
    connect(sourceDataLine);
  }

  private void connect(SourceDataLine sourceDataLine) {
    client.websocket(new RequestOptions().setHost("kyngas.eu").setPort(443).setSsl(false).setURI("/ws"), socket -> {
      log.info("Connected to server");
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
