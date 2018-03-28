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

import eu.kyngas.grapes.proxy.ProxyServiceImpl;
import eu.kyngas.grapes.common.util.Ctx;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class SpotifyMusicServiceImpl extends ProxyServiceImpl<SpotifyMusicService> implements SpotifyMusicService {
  private final JsonObject config;
  private final Vertx vertx = Ctx.vertx();

  SpotifyMusicServiceImpl(JsonObject config) {
    super(ADDRESS, SpotifyMusicService.class);
    this.config = config;
  }

  @Override
  public SpotifyMusicService getTestData(Handler<AsyncResult<JsonObject>> handler) {
    handler.handle(Future.succeededFuture(new JsonObject().put("data", "value")));
    return this;
  }
}
