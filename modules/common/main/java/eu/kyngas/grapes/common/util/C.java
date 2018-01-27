package eu.kyngas.grapes.common.util;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class C {

  public static boolean ifTrue(boolean check, Runnable ifTrue) {
    if (check) {
      ifTrue.run();
    }
    return check;
  }

  public static boolean ifFalse(boolean check, Runnable ifFalse) {
    return ifTrue(!check, ifFalse);
  }

  public static boolean check(boolean check, Runnable ifTrue, Runnable ifFalse) {
    if (check) {
      ifTrue.run();
    } else {
      ifFalse.run();
    }
    return check;
  }
}
