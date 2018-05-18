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
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class F {
  private static final long FUTURE_TIMEOUT = TimeUnit.SECONDS.toMillis(30);

  public static <T> Future<T> fail(String format, Object... params) {
    return Future.failedFuture(String.format(format, params));
  }

  public static <T> Future<T> fail(Throwable throwable) {
    return Future.failedFuture(throwable);
  }

  public static <T> Future<T> fail(String msg, Throwable... throwables) {
    Throwable thr = new Throwable(msg);
    Arrays.stream(throwables).forEach(thr::addSuppressed);
    return Future.failedFuture(thr);
  }

  public static <T> Future<T> success() {
    return Future.succeededFuture();
  }

  public static <T> Future<T> success(T result) {
    return Future.succeededFuture(result);
  }

  public static <T, S> Future<S> map(Future<T> future, Function<AsyncResult<T>, AsyncResult<S>> mapper) {
    return F.future(fut -> future.setHandler(ar -> fut.handle(mapper.apply(ar))));
  }

  public static <T> Future<T> future(Callback.Returning<T> callback) {
    return Future.future(fut -> {
      try {
        fut.complete(callback.exec());
      } catch (Exception e) {
        fut.fail(e);
      }
    });
  }

  public static <T> Future<T> future(Callback.Paramless callback) {
    return Future.future(fut -> {
      try {
        if (callback != null) {
          callback.exec();
        }
        fut.tryComplete();
      } catch (Exception e) {
        fut.tryFail(e);
      }
    });
  }

  public static <T> Future<T> future(Callback.Param<Future<T>> callback) {
    return Future.future(fut -> {
      try {
        callback.exec(fut);
        if (!fut.isComplete()) {
          Ctx.sleep(Ctx.isProductionMode() ? FUTURE_TIMEOUT : TimeUnit.MINUTES.toMillis(10), t -> {
            if (!fut.isComplete()) {
              fut.fail("Future has timed out.");
            }
          });
        }
      } catch (Exception e) {
        fut.tryFail(e);
      }
    });
  }

  public static <T> Future<T> futureThrowsException(Callback.Returning<Future<T>> callback) {
    return Future.future(fut -> {
      try {
        fut.handle(callback.exec());
      } catch (Exception e) {
        fut.tryFail(e);
      }
    });
  }

  /**
   * Success -> all futures succeeded
   * Failure -> any future failed, fails on first failed future (fail fast)
   */
  public static CompositeFuture all(Future... futures) {
    return CompositeFuture.all(Arrays.asList(futures));
  }

  public static CompositeFuture all(List<Future> futures) {
    return CompositeFuture.all(futures);
  }

  /**
   * Success -> any future succeeded, succeeds on first succeeded future (succeed fast)
   * Failure -> all futures failed
   */
  public static CompositeFuture any(Future... futures) {
    return CompositeFuture.any(Arrays.asList(futures));
  }

  /**
   * Success -> all futures succeeded
   * Failure -> any future failed, waits until all futures completed (fail slow)
   */
  public static CompositeFuture join(Future... futures) {
    return CompositeFuture.join(Arrays.asList(futures));
  }
}
