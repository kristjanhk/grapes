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

import eu.kyngas.grapes.common.router.Status;
import eu.kyngas.grapes.music.router.RestRouter;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class SpotifyRouter extends RestRouter {
  private final SpotifyAuthService spotifyAuthService = SpotifyAuthService.create();
  private final SpotifyMusicService spotifyMusicService = SpotifyMusicService.create();
  private final SpotifyLocalService spotifyLocalService = SpotifyLocalService.create();

  @Override
  public void addRoutes() {
    get("/authorize").handler(ctx -> spotifyAuthService.doAuthorize(handler(ctx, action -> action.redirect(ctx))));
    get("/callback").handler(ctx -> spotifyAuthService.doCallback(ctx, jsonResponse(ctx)));
    get("/toggle").handler(ctx -> spotifyLocalService.togglePlayback(handler(ctx, v -> Status.ok(ctx))));
    get("/openuri").handler(ctx -> {
      String uri = ctx.request().getParam("uri");
      if (uri == null) {
        Status.badRequest(ctx, new Throwable("Missing uri parameter"));
        return;
      }
      spotifyLocalService.playTrack(uri, handler(ctx, v -> Status.ok(ctx)));
    });
  }
}
