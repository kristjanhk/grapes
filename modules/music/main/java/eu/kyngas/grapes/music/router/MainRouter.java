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

package eu.kyngas.grapes.music.router;

import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.music.spotify.SpotifyRouter;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.impl.RouterImpl;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class MainRouter extends RouterImpl {
  private final RestRouter spotifyRouter = new SpotifyRouter();

  private MainRouter() {
    super(Ctx.vertx());
  }

  public static Router create() {
    return new MainRouter().initRoutes();
  }

  public Router initRoutes() {
    // TODO: 1.02.2018 impl common routes -> static handler etc.
    spotifyRouter.subRouterTo(this, "/music");
    return this;
  }
}
