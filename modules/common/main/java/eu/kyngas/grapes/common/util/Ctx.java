package eu.kyngas.grapes.common.util;

import eu.kyngas.grapes.common.entity.Callback;
import eu.kyngas.grapes.common.entity.JsonObj;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import java.util.concurrent.TimeUnit;
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
    create(new VertxOptions(), consumer);
  }

  public static void create(VertxOptions options, Consumer<Vertx> consumer) {
    if (!Config.isRunningFromJar()) {
      options.setBlockedThreadCheckInterval(TimeUnit.MINUTES.toMillis(10));
    }
    N.safe(options, opts -> N.safe(consumer, c -> c.accept(Vertx.vertx(opts))));
  }

  public static void async(Runnable task) {
    ctx().runOnContext(v -> task.run());
  }

  public static <T> Future<T> blocking(Callback.Returning<T> callback) {
    return F.future(fut -> ctx().executeBlocking(h -> F.future(callback).setHandler(h), fut));
  }

  public static Future<Void> blocking(Callback.Paramless callback) {
    return F.future(fut -> ctx().executeBlocking(h -> F.<Void>future(callback).setHandler(h), fut));
  }

  public static boolean isProductionMode() {
    return Networks.getInstance().isProduction();
  }

  public static boolean getTestingMode() {
    return TESTING_MODE.get();
  }

  public static void setTestingMode(boolean testingMode) {
    TESTING_MODE.set(testingMode);
  }

  public static void sleep(long ms, Handler<Long> timer) {
    vertx().setTimer(ms, timer);
  }

  public static void periodic(long ms, Handler<Long> timer) {
    vertx().setPeriodic(ms, timer);
  }
}
