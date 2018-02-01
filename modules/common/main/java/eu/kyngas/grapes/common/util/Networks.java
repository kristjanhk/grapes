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

import io.vertx.core.json.JsonObject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Networks {
  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_PORT = 80;
  public static final String HTTP_PORT = "http_port";
  public static final String HTTP_HOST = "http_host";
  public static final String HTTP_SSL = "http_ssl";
  public static final int MAX_BODY_SIZE = 25 * 1024 * 1024; //25mb

  private static final String PRODUCTION_HOST = "production_host";
  private static final String PRODUCTION_PORT = "production_port";
  private static Networks INSTANCE;

  private final JsonObject config = Ctx.config();
  private final String productionHost = config.getString(PRODUCTION_HOST, DEFAULT_HOST);
  private final int productionPort = config.getInteger(PRODUCTION_PORT, DEFAULT_PORT);
  private Boolean isInProduction;

  public static Networks getInstance() {
    return INSTANCE != null ? INSTANCE : (INSTANCE = new Networks());
  }

  /**
   * Checks if given host is resolved to localhost.
   *
   * @param host to resolve
   * @return boolean
   */
  private boolean isLoopbackInterface(String host) {
    if (host != null && !host.isEmpty() && Eq.ne(host, "localhost")) {
      try {
        return InetAddress.getByName(host).isLoopbackAddress();
      } catch (UnknownHostException e) {
        log.error("Failed to check for loopback address.", e);
      }
    }
    return false;
  }

  /**
   * Checks if configured production host is resolved to localhost.
   *
   * @return boolean
   */
  public boolean isProduction() {
    return isInProduction != null
        ? isInProduction
        : (isInProduction = isLoopbackInterface(productionHost));
  }

  public String getProductionHost() {
    return isProduction() ? toFullUrl(productionHost, productionPort) : toFullUrl(DEFAULT_HOST, DEFAULT_PORT);
  }

  private String toFullUrl(String host, int port) {
    return String.format("http%s://%s", port == 443 ? "s" : "", host);
  }
}
