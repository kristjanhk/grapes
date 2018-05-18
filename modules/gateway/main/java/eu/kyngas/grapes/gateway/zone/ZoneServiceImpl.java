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

package eu.kyngas.grapes.gateway.zone;

import eu.kyngas.grapes.common.entity.JsonObj;
import eu.kyngas.grapes.common.util.Eq;
import eu.kyngas.grapes.common.util.F;
import eu.kyngas.grapes.common.util.S;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import static eu.kyngas.grapes.common.router.Status.CREATED;
import static eu.kyngas.grapes.common.router.Status.INVALID_INPUT;
import static eu.kyngas.grapes.common.router.Status.NOCONTENT;
import static eu.kyngas.grapes.common.util.Http.*;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class ZoneServiceImpl implements ZoneService {
  private static final String PREFIX = "_acme-challenge.";
  private final JsonObject config;
  private final HttpClient client;
  private final Map<String, ZoneTxtRecord> txtRecordsByHost = new HashMap<>();

  public ZoneServiceImpl(JsonObject config) {
    this.config = config;
    this.client = createClient(config);
  }

  @Override
  public Future<ZoneTxtRecord> createDnsTxtRecord(String host, String destination) {
    return F.future(future -> {
      HttpClientRequest request = client.post(String.format("/v2/dns/%s/txt", host), handlePostRecord(host, future));
      JsonObject body = new JsonObject().put("name", PREFIX + host).put("destination", destination);
      logEnd(addBasicAuth(sendJson(request, body)));
    });
  }

  private Handler<HttpClientResponse> handlePostRecord(String host, Future<ZoneTxtRecord> fut) {
    return res -> {
      if (Eq.ne(res.statusCode(), CREATED, INVALID_INPUT)) {
        fut.fail(S.format("Failed to create DNS TXT record, code: %s, message: %s",
                          res.statusCode(),
                          res.statusMessage()));
        return;
      }
      res.bodyHandler(body -> {
        JsonObj json = JsonObj.toCamelCase(body.toJsonArray().getJsonObject(0));
        if (res.statusCode() == INVALID_INPUT) {
          fut.fail("Failed to create DNS TXT record, invalid input: " + json.encodePrettily());
          return;
        }
        ZoneTxtRecord record = json.mapTo(ZoneTxtRecord.class);
        txtRecordsByHost.put(host, record);
        fut.complete(record);
      });
    };
  }

  @Override
  public Future<Void> deleteDnsTxtRecord(String host) {
    return F.future(future -> {
      ZoneTxtRecord record = txtRecordsByHost.remove(host);
      if (record == null) {
        future.fail(S.format("Failed to delete DNS TXT record, record for host %s not found.", host));
        return;
      }
      HttpClientRequest request =
          client.delete(String.format("/v2/dns/%s/txt/%s", host, record.getId()), handleDeleteRecord(future));
      logEnd(addBasicAuth(request));
    });
  }

  private Handler<HttpClientResponse> handleDeleteRecord(Future<Void> fut) {
    return res -> {
      if (res.statusCode() != NOCONTENT) {
        fut.fail(S.format("Failed to create DNS TXT record, code: %s, message: %s",
                          res.statusCode(),
                          res.statusMessage()));
        return;
      }
      fut.complete();
    };
  }

  @Override
  public void close() {
    client.close();
  }

  private HttpClientRequest addBasicAuth(HttpClientRequest request) {
    return basicAuth(request, config.getString("username"), config.getString("password"));
  }
}
