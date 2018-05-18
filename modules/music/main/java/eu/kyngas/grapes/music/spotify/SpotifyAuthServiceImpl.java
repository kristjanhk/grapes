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

import eu.kyngas.grapes.common.entity.JsonObj;
import eu.kyngas.grapes.common.entity.Pair;
import eu.kyngas.grapes.common.router.RedirectAction;
import eu.kyngas.grapes.common.router.Status;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.F;
import eu.kyngas.grapes.common.util.H;
import eu.kyngas.grapes.common.util.Logs;
import eu.kyngas.grapes.common.util.Networks;
import eu.kyngas.grapes.common.util.S;
import eu.kyngas.grapes.common.util.Unsafe;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static eu.kyngas.grapes.common.util.Http.*;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class SpotifyAuthServiceImpl implements SpotifyAuthService {
  private static final String CLIENT_ID = "client_id";
  private static final String CLIENT_SECRET = "client_secret";
  private static final String REDIRECT_URI = "redirect_uri";
  private static final String CODE = "code";
  private static final String STATE = "state";
  private static final String ERROR = "error";
  private static final String GRANT_TYPE = "grant_type";
  private static final String REFRESH_TOKEN = "refresh_token";

  private final Vertx vertx = Ctx.vertx();
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;
  private final HttpClient client;

  private Set<String> csrfTokens = new HashSet<>();
  private Map<String, Token> tokens = new HashMap<>();

  SpotifyAuthServiceImpl(JsonObject config) {
    this.clientId = config.getString(CLIENT_ID, "");
    this.clientSecret = config.getString(CLIENT_SECRET, "");
    this.redirectUri = config.getString(REDIRECT_URI, "");
    this.client = createClient(config);
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
    csrfTokens.add(uuid);
    handler.handle(Future.succeededFuture(Query.of("/authorize")
                                              .param(CLIENT_ID, clientId)
                                              .param("response_type", CODE)
                                              .param(REDIRECT_URI, getRedirectUri())
                                              .param(STATE, uuid)
                                              .toRedirectAction(Unsafe.<HttpClientImpl>cast(client).getOptions())));
    vertx.setTimer(TimeUnit.MINUTES.toMillis(2), t -> csrfTokens.remove(uuid));
    return this;
  }

  //todo pass queryparams instead or smth
  @Override
  public SpotifyAuthService doCallback(RoutingContext ctx, Handler<AsyncResult<JsonObject>> handler) {
    if (!csrfTokens.remove(ctx.queryParams().get(STATE))) {
      handler.handle(Future.failedFuture("CSRF token expired."));
      return this;
    }
    String error = ctx.queryParams().get(ERROR);
    if (error != null) {
      handler.handle(Future.failedFuture("Spotify auth callback ex: " + error));
      return this;
    }
    return requestToken(ctx.queryParams().get(CODE), handler);
  }

  private SpotifyAuthService requestToken(String code, Handler<AsyncResult<JsonObject>> handler) {
    String uri = Query.of("/api/token")
        .param(GRANT_TYPE, "authorization_code")
        .param(CODE, code)
        .param(REDIRECT_URI, getRedirectUri())
        .create();
    logEnd(basicAuth(client.post(uri, res -> handleRequestToken(res, handler)), clientId, clientSecret));
    return this;
  }

  private void handleRequestToken(HttpClientResponse res, Handler<AsyncResult<JsonObject>> handler) {
    if (res.statusCode() != Status.OK) {
      handler.handle(F.fail("Spotify token request failed: status %s, message %s",
                            res.statusCode(),
                            res.statusMessage()));
      return;
    }
    res.bodyHandler(H.toLogJson(json -> tokens.put("dummy", JsonObj.mapTo(json, Token.class))));
    //todo replace dummy with actual user
  }

  private String getRedirectUri() {
    return Networks.getInstance().getProductionHost() + redirectUri;
  }

  private String getAuthorizationHeader() {
    return S.base64("%s:%s", clientId, clientSecret);
  }

  private SpotifyAuthService requestRefreshToken() {
    String uri = Query.of("/api/token")
        .param(GRANT_TYPE, REFRESH_TOKEN)
        .param(REFRESH_TOKEN, tokens.get("dummy").getRefreshToken())
        .create();
    HttpClientRequest request = client.post(uri, this::handleRequestRefreshToken)
        .putHeader(HttpHeaders.AUTHORIZATION, getAuthorizationHeader())
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.APPLICATION_X_WWW_FORM_URLENCODED);
    logEnd(request);
    return this;
  }

  private void handleRequestRefreshToken(HttpClientResponse res) {
    if (res.statusCode() != Status.OK) {
      Logs.error("Spotify token refresh request failed: status {}, message {}", res.statusCode(), res.statusMessage());
      return;
    }
    res.bodyHandler(H.toLogJson(json -> tokens.compute("dummy", (key, previousToken) -> JsonObj
        .mapTo(json, Token.class)
        .setRefreshToken(previousToken == null ? null : previousToken.getRefreshToken()))));
  }
}
