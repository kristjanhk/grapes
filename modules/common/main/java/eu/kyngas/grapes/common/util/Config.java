package eu.kyngas.grapes.common.util;

import io.vertx.core.json.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public class Config {
  private static final String DEFAULT_CONFIG = "/config.json";
  private static final boolean IS_RUNNING_FROM_JAR = getLocation().toString().endsWith(".jar");

  /**
   * Loads config from classpath.
   *
   * @return config.json config
   */
  public static JsonObject getConfig() {
    return getConfig(DEFAULT_CONFIG);
  }

  public static JsonObject getConfig(String location) {
    try {
      return new JsonObject(readToString(checkFormat(location)));
    } catch (IOException e) {
      log.error(location + " not found.");
    }
    return new JsonObject();
  }

  private static String checkFormat(String location) {
    if (location == null) {
      return DEFAULT_CONFIG;
    }
    if (location.charAt(0) != '/') {
      location = "/" + location;
    }
    if (!location.endsWith(".json")) {
      location += ".json";
    }
    return location;
  }

  /**
   * Reads file on given location on classpath into string.
   *
   * @param location to read from
   * @return file at that location as string
   * @throws IOException when not found
   */
  private static String readToString(String location) throws IOException {
    return readToString(Config.class.getResourceAsStream(location));
  }

  /**
   * Reads inputStream into string.
   *
   * @param inputStream to read
   * @return stream as string
   * @throws IOException when not found
   */
  private static String readToString(InputStream inputStream) throws IOException {
    if (inputStream == null) {
      throw new FileNotFoundException();
    }
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = inputStream.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    inputStream.close();
    return result.toString("UTF-8");
  }

  private static Path getLocation() {
    return Paths.get(Config.class.getProtectionDomain()
                                 .getCodeSource()
                                 .getLocation()
                                 .getPath()
                                 .substring(1));
  }

  public static boolean isRunningFromJar() {
    return IS_RUNNING_FROM_JAR;
  }
}
