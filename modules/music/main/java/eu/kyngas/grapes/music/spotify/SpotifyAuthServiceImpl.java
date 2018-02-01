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

import eu.kyngas.grapes.common.entity.Pair;
import eu.kyngas.grapes.common.router.RedirectAction;
import eu.kyngas.grapes.common.service.HttpService;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.Http;
import eu.kyngas.grapes.common.util.Networks;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class SpotifyAuthServiceImpl implements SpotifyAuthService, HttpService {
  private static final String CLIENT_ID = "client_id";
  private static final String CLIENT_SECRET = "client_secret";
  private static final String REDIRECT_URI = "redirect_uri";

  private final Vertx vertx = Ctx.vertx();
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;
  private final HttpClientOptions options;
  private final HttpClient client;

  private Set<String> knownUuids = new HashSet<>();

  public SpotifyAuthServiceImpl(JsonObject config) {
    this.clientId = config.getString(CLIENT_ID, "");
    this.clientSecret = config.getString(CLIENT_SECRET, "");
    this.redirectUri = config.getString(REDIRECT_URI, "");
    this.options = new HttpClientOptions()
        .setDefaultHost(config.getString(HOST, "accounts.spotify.com"))
        .setDefaultPort(config.getInteger(PORT, 443))
        .setSsl(config.getBoolean(SSL, true));
    this.client = vertx.createHttpClient(options);
  }

  @Override
  public SpotifyAuthService doAuthorize(Handler<AsyncResult<RedirectAction>> handler) {
    String missingConfigurations = Stream.of(Pair.of(CLIENT_ID, clientId),
                                             Pair.of(CLIENT_SECRET, clientSecret),
                                             Pair.of(REDIRECT_URI, redirectUri))
        .filter(pair -> pair.getSnd().isEmpty())
        .map(Pair::getFst)
        .collect(Collectors.joining(","));
    if (!missingConfigurations.isEmpty()) {
      handler.handle(Future.failedFuture(String.format("SpotifyService is missing variables: %s",
                                                       missingConfigurations)));
      return this;
    }
    String uuid = UUID.randomUUID().toString();
    knownUuids.add(uuid);
    String redirectUri = Networks.getInstance().getProductionHost() + this.redirectUri;
    handler.handle(Future.succeededFuture(Http.Query.of("/authorize")
                                              .param(CLIENT_ID, clientId)
                                              .param("response_type", "code")
                                              .param(REDIRECT_URI, redirectUri)
                                              .param("state", uuid)
                                              .toRedirectAction(options)));
    vertx.setTimer(TimeUnit.MINUTES.toMillis(5), t -> knownUuids.remove(uuid));
    return this;
  }

  public SpotifyAuthService doAuthorizeCallback() {

    return this;
  }
}
