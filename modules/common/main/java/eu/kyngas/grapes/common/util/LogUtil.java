package eu.kyngas.grapes.common.util;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class LogUtil {

  /**
   * Changes vertx logging to SLF4J.
   */
  public static void setLoggingToSLF4J() {
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
  }
}
