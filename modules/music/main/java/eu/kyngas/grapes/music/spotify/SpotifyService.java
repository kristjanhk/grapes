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

import eu.kyngas.grapes.common.util.Config;
import eu.kyngas.grapes.common.util.Ctx;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public interface SpotifyService {

  static SpotifyAuthService createAuthService() {
    return new SpotifyAuthServiceImpl(Ctx.subConfig("spotify", "auth")
                                          .deepMergeIn(Config.getJson("spotify-secret")));
  }

  static SpotifyLocalService createLocalService() {
    return new SpotifyLocalServiceImpl();
  }

  static SpotifyMusicService createMusicService() {
    return new SpotifyMusicServiceImpl(Ctx.subConfig("spotify"));
  }
}
