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

package eu.kyngas.grapes.common.router;

import eu.kyngas.grapes.common.util.Logs;
import eu.kyngas.grapes.common.util.Strings;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public interface RestRouter extends Router {

  String getPath();

  void addRoutes();

  default void subRouterTo(Router parent, String path) {
    parent.mountSubRouter(path, this);
    addRoutes();
    Logs.debug("{} registered routes under {}: {}",
               getClass().getSimpleName(), path, Strings.join(getRoutes(), Route::getPath));
  }

  default void close() {

  }
}
