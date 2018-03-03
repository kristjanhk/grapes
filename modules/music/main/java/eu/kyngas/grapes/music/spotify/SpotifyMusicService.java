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

import eu.kyngas.grapes.common.service.ProxyService;
import eu.kyngas.grapes.common.util.Ctx;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@VertxGen
@ProxyGen
public interface SpotifyMusicService extends SpotifyService {
  String ADDRESS = "music.spotify.music";

  static SpotifyMusicService create() {
    return new SpotifyMusicServiceImpl(Ctx.subConfig(SPOTIFY));
  }

  static SpotifyMusicService createProxy() {
    return ProxyService.createProxy(ADDRESS, SpotifyMusicService.class);
  }

  @Fluent
  SpotifyMusicService getTestData(Handler<AsyncResult<JsonObject>> handler);

  @ProxyClose
  void close();
}
