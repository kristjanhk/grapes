package eu.kyngas.grapes.common.util;

import eu.kyngas.grapes.common.entity.JsonObj;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Ctx {
  private static final AtomicBoolean TESTING_MODE = new AtomicBoolean(false);

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

  public static JsonObj config() {
    return JsonObj.from(ctx().config());
  }

  public static JsonObj subConfig(String... subKeys) {
    return Config.getSubConfig(config(), subKeys);
  }

  public static void create(Consumer<Vertx> consumer) {
    N.safe(consumer, c -> c.accept(Vertx.vertx()));
  }

  public static void async(Runnable task) {
    ctx().runOnContext(v -> task.run());
  }

  public static <T> void blocking(Handler<Future<T>> blockingHandler, Handler<AsyncResult<T>> resultHandler) {
    ctx().executeBlocking(blockingHandler, resultHandler);
  }

  public static boolean getTestingMode() {
    return TESTING_MODE.get();
  }

  public static void setTestingMode(boolean testingMode) {
    TESTING_MODE.set(testingMode);
  }
}
