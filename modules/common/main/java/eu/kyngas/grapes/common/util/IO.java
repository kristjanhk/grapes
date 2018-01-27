package eu.kyngas.grapes.common.util;

import java.io.Closeable;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public class IO {

  public static void close(Closeable closeable) {
    try {
      closeable.close();
    } catch (IOException e) {
      log.error("Failed to close IO resource", e);
    }
  }
}
