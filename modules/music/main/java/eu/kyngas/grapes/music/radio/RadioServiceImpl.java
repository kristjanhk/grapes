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

package eu.kyngas.grapes.music.radio;

import eu.kyngas.grapes.common.router.Status;
import eu.kyngas.grapes.common.util.C;
import eu.kyngas.grapes.common.util.Streams;
import eu.kyngas.grapes.music.util.AsyncInputStream;
import eu.kyngas.grapes.music.util.AudioUtil;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.lame.lowlevel.LameEncoder;
import net.sourceforge.lame.mp3.Lame;
import static eu.kyngas.grapes.music.util.AudioUtil.MIXER_INDEX;
import static eu.kyngas.grapes.music.util.AudioUtil.createEncoder;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public class RadioServiceImpl implements RadioService {
  private final Map<String, RoutingContext> clients = new HashMap<>();
  private final JsonObject config;
  private TargetDataLine line;
  private LameEncoder encoder;

  RadioServiceImpl(JsonObject config) {
    this.config = config;
  }

  @Override
  public void addClient(RoutingContext ctx) {
    HttpServerResponse response = ctx.response().setChunked(true);
    String clientId = UUID.randomUUID().toString(); //todo get user id from ctx
    response.closeHandler(v -> handleClientDisconnect(clientId));

    clients.put(clientId, ctx);
    if (clients.size() > 1) {
      log.info("Client {} connected -- reusing audio recording thread.", clientId);
      return;
    }
    log.info("Client {} connected -- starting new audio recording thread.", clientId);

    try {
      line = AudioUtil.startAudioRecording(config.getInteger(MIXER_INDEX));
    } catch (LineUnavailableException e) {
      log.error("Failed to open TargetDataLine -- rejecting client.", e);
      Status.badGateway(ctx, e);
      return;
    }

    encoder = createEncoder(line.getFormat(),
                            config.getInteger("bitrate", 128),
                            config.getInteger("quality", Lame.QUALITY_MIDDLE));

    AsyncInputStream in = new AsyncInputStream(new AudioInputStream(line));
    in.endHandler(v -> C.check(clients.isEmpty(), () -> log.info("Audio recording input closed."), () -> {
      log.error("Audio recording input closed -- disconnecting all clients.");
      Streams.mapToList(clients.values(), RoutingContext::response).forEach(HttpServerResponse::close);
    }));
    in.exceptionHandler(e -> log.error("Exception in audio recording.", e));
    in.handler(AudioUtil.encode(encoder, buffer -> Streams
        .mapToList(clients.values(), RoutingContext::response)
        .forEach(res -> C.check(!res.writeQueueFull(),
                                () -> res.write(buffer),
                                () -> log.debug("Client {}: write queue full", clientId)))));
  }

  private void handleClientDisconnect(String clientId) {
    clients.remove(clientId);
    log.info("Client {} disconnected", clientId);
    C.ifTrue(clients.isEmpty(), () -> {
      log.info("All clients disconnected -- closing TargetDataLine recording");
      line.close();
      encoder.close();
    });
  }
}
