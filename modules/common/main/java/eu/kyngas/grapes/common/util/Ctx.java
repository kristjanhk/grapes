package eu.kyngas.grapes.common.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.function.Consumer;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class Ctx {

  public static Context ctx() {
    Context context = Vertx.currentContext();
    if (context == null) {
      throw new IllegalStateException("Vertx context is called from non-vertx thread!");
    }
    return context;
  }

  public static Vertx vertx() {
    return ctx().owner();
  }

  public static JsonObject config() {
    return Ctx.ctx().config();
  }

  public static JsonObject subConfig(String... subKeys) {
    return Config.getSubConfig(config(), subKeys);
  }

  public static void createVertx(Consumer<Vertx> consumer) {
    N.safe(consumer, c -> c.accept(Vertx.vertx()));
  }

  public static Vertx async(Runnable task) {
    Context ctx = ctx();
    ctx.runOnContext(v -> task.run());
    return ctx.owner();
  }

  public static <T> Vertx asyncBlocking(Handler<Future<T>> blockingHandler, Handler<AsyncResult<T>> resultHandler) {
    Context ctx = ctx();
    ctx.executeBlocking(blockingHandler, resultHandler);
    return ctx.owner();
  }
}
