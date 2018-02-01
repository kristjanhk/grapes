package eu.kyngas.grapes.common.util;

import java.util.stream.Stream;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class C {

  public static boolean ifTrue(boolean check, Runnable ifTrue, Runnable... andThen) {
    if (check) {
      ifTrue.run();
      return true;
    }
    Stream.of(andThen).forEach(Runnable::run);
    return false;
  }

  public static boolean ifFalse(boolean check, Runnable ifFalse, Runnable... andThen) {
    if (!check) {
      ifFalse.run();
      return false;
    }
    Stream.of(andThen).forEach(Runnable::run);
    return true;
  }

  public static boolean check(boolean check, Runnable ifTrue, Runnable ifFalse, Runnable... andThen) {
    if (check) {
      ifTrue.run();
    } else {
      ifFalse.run();
    }
    Stream.of(andThen).forEach(Runnable::run);
    return check;
  }
}
