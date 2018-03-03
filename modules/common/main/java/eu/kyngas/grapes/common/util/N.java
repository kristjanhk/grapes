package eu.kyngas.grapes.common.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public class N {

  public static <T> T safe(T obj, Consumer<T> consumer) {
    if (obj != null) {
      consumer.accept(obj);
    }
    return obj;
  }

  public static <T> T safe(T obj, Runnable runnable) {
    if (obj != null) {
      runnable.run();
    }
    return obj;
  }

  public static <T> T ifExists(T in, Supplier<T> ifExists) {
    return in != null ? ifExists.get() : null;
  }

  public static <T> T ifNotExists(T in, Supplier<T> ifNotExists) {
    return in == null ? ifNotExists.get() : in;
  }

  public static boolean anyNull(Object... objs) {
    return Arrays.stream(objs).anyMatch(Objects::isNull);
  }

  public static boolean noneNull(Object... objs) {
    return Arrays.stream(objs).noneMatch(Objects::isNull);
  }
}
