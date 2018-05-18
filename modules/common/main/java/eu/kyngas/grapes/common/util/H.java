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

import eu.kyngas.grapes.common.entity.Callback;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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

  public static Handler<AsyncResult<String>> handleVerticleStarted(Vertx vertx, String moduleName) {
    return ar -> C.check(ar.succeeded(), () -> Logs.info(4, "{} module started.", moduleName), () -> {
      Logs.error(4, "Failed to start {} module.", moduleName, ar.cause());
      vertx.close();
    });
  }

  public static Handler<AsyncResult<HttpServer>> handleServerStarted(Future<Void> fut) {
    return ar -> {
      if (ar.failed()) {
        Logs.error("Failed to start http server.", ar.cause());
        fut.fail(ar.cause());
        return;
      }
      fut.complete();
    };
  }

  public static ExHandler handler(Handler exHandler) {
    return new ExHandler(Unsafe.cast(exHandler));
  }

  @RequiredArgsConstructor
  public static class ExHandler {
    private final Future<?> exFuture;
    private String failMsg = "Unknown exception";

    public ExHandler failMsg(String failMsg) {
      this.failMsg = failMsg;
      return this;
    }

    public <T> Future<T> handle(@NonNull Runnable runnable) {
      return handle(v -> runnable.run());
    }

    public <T> Future<T> handle(@NonNull Consumer<T> consumer) {
      return F.future(ar -> {
        if (ar.failed()) {
          exFuture.handle(F.fail(failMsg, ar.cause()));
          return;
        }
        consumer.accept(ar.result());
      });
    }

    public void handleException(@NonNull Callback.Paramless callback) {
      try {
        callback.exec();
      } catch (Exception e) {
        exFuture.handle(F.fail(failMsg, e));
      }
    }
  }
}
