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

package eu.kyngas.grapes.music.spotify;

import eu.kyngas.grapes.common.util.Logs;
import eu.kyngas.grapes.common.router.AbstractRouter;
import eu.kyngas.grapes.common.router.RestRouter;
import eu.kyngas.grapes.common.router.SockJsRouter;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import java.util.List;
import static io.vertx.ext.bridge.BridgeEventType.RECEIVE;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class SpotifyRouter extends AbstractRouter implements RestRouter, SockJsRouter {
  private final SpotifyAuthService spotifyAuthService = SpotifyAuthService.create();
  private final SpotifyMusicService spotifyMusicService = SpotifyMusicService.create();
  private final SpotifyLocalService spotifyLocalService = SpotifyLocalService.create();

  @Override
  public String getPath() {
    return "/spotify";
  }

  @Override
  public void addRoutes() {
    get("/authorize").handler(ctx -> spotifyAuthService.doAuthorize(handler(ctx, action -> action.redirect(ctx))));
    get("/callback").handler(ctx -> spotifyAuthService.doCallback(ctx, jsonResponse(ctx)));
  }

  @Override
  public void close() {
    spotifyMusicService.close();
    spotifyLocalService.close();
  }

  @Override
  public void permitRoutes(List<PermittedOptions> inbound, List<PermittedOptions> outbound) {
    inbound.add(new PermittedOptions().setAddress(SpotifyLocalService.ADDRESS));
    inbound.add(new PermittedOptions().setAddress(SpotifyMusicService.ADDRESS));
  }

  @Override
  public void intercept(BridgeEvent event) {
    if (event.getRawMessage() != null && event.type() != RECEIVE) {
      Logs.debug("Sockjs: {}", event.getRawMessage().encodePrettily());
    }
  }
}
