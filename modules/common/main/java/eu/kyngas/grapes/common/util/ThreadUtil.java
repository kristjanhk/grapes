package eu.kyngas.grapes.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public class ThreadUtil {

  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      log.error("Sleeping thread interrupted", e);
      // TODO: 25.01.2018 should rethrow interrupt
    }
  }
}
