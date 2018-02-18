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

package eu.kyngas.grapes.common.util;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import java.util.function.Consumer;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class H {

  public static Handler<Buffer> toLogJson(Consumer<JsonObject> consumer) {
    return buffer -> {
      JsonObject json = buffer.toJsonObject();
      Logs.info("Response json: {}", json);
      N.safe(consumer, c -> c.accept(json));
    };
  }

  public static Handler<HttpClientResponse> logResponse(Consumer<HttpClientResponse> consumer) {
    return response -> N.safe(consumer, c -> {

    });
  }
}
