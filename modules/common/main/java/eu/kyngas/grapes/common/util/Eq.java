package eu.kyngas.grapes.common.util;

import java.util.Arrays;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class Eq {

  @SafeVarargs
  public static <T> boolean eq(T checkable, T... against) {
    return Arrays.asList(against).contains(checkable);
  }

  @SafeVarargs
  public static <T> boolean ne(T checkable, T... against) {
    return !eq(checkable, against);
  }
}
