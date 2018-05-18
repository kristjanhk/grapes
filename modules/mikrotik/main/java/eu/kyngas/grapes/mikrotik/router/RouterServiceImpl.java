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

package eu.kyngas.grapes.mikrotik.router;

import eu.kyngas.grapes.common.entity.Callback;
import eu.kyngas.grapes.common.util.C;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.F;
import eu.kyngas.grapes.common.util.Logs;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.TimeUnit;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.ApiConnectionException;
import me.legrange.mikrotik.MikrotikApiException;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class RouterServiceImpl implements RouterService {
  private static final long TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(5);
  private final String lteRestartFormat;

  private final Vertx vertx;
  private ApiConnection api;

  public RouterServiceImpl(JsonObject config) {
    this.vertx = Ctx.vertx();
    this.lteRestartFormat = "/interface/lte/set .id=" + config.getString("lte-interface") + " disabled=%s";
    try {
      this.api = ApiConnection.connect(config.getString("router-host"));
      this.api.setTimeout((int) TIMEOUT_MILLIS);
      this.api.login(config.getString("username"), config.getString("password"));
    } catch (MikrotikApiException e) {
      Logs.error("Failed to open connection to router.", e);
      vertx.close();
    }
  }

  @Override
  public void restartLte(Handler<AsyncResult<Void>> handler) {
    // TODO: 02/03/2018 test replace macid -> generate new one -> api call replace lte macid
    Logs.info("Disabling LTE");
    C.ifTrue(handleApiCall(() -> api.execute(String.format(lteRestartFormat, "yes")), handler),
             () -> enableLte(handler));
  }

  private void enableLte(Handler<AsyncResult<Void>> handler) {
    vertx.setTimer(TIMEOUT_MILLIS, timerId -> {
      Logs.info("Enabling LTE");
      handleApiCall(() -> api.execute(String.format(lteRestartFormat, "no")), handler);
      vertx.setTimer(TIMEOUT_MILLIS, timerId2 -> {
        Logs.info("LTE restart done.");
        handler.handle(F.success());
      });
    });
  }

  @Override
  public void restartSystem(Handler<AsyncResult<Void>> handler) {
    handleApiCall(() -> {
      api.execute("/system/reboot");
      handler.handle(F.success());
    }, handler);
  }

  @Override
  public void close(Handler<Void> handler) {
    try {
      api.close();
    } catch (ApiConnectionException e) {
      Logs.error("Failed to close connection to router.", e);
      vertx.close();
    }
  }

  private boolean handleApiCall(Callback.Paramless callback, Handler<AsyncResult<Void>> handler) {
    try {
      callback.exec();
      return true;
    } catch (Exception e) {
      handler.handle(F.fail(e));
    }
    return false;
  }
}
