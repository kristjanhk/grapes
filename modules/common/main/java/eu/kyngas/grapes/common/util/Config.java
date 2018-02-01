package eu.kyngas.grapes.common.util;

import eu.kyngas.grapes.common.entity.JsonObj;
import io.vertx.core.json.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public class Config {
  private static final String DEFAULT_CONFIG = "/global.json";
  private static final boolean IS_RUNNING_FROM_JAR = getLocation().toString().endsWith(".jar");

  public static JsonObj getConfig(String location, String[] args) {
    return getConfig(location).mergeIn(getArgs(args));
  }

  public static JsonObj getConfig(String[] args) {
    return getConfig(DEFAULT_CONFIG, args);
  }

  public static JsonObj getGlobal() {
    return getConfig(DEFAULT_CONFIG);
  }

  public static JsonObj getConfig(String location) {
    try {
      return new JsonObj(readToString(checkFormat(location)));
    } catch (IOException e) {
      log.error("Config {} not found.", location);
    }
    return new JsonObj();
  }

  public static JsonObj getSubConfig(JsonObject config, String... subKeys) {
    for (String key : subKeys) {
      if (!config.containsKey(key)) {
        log.error("Subconfig with does not exist with keys {}.", Arrays.toString(subKeys));
        return new JsonObj();
      }
      config = config.getJsonObject(key);
    }
    return JsonObj.from(config);
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

  /**
   * Converts given args to JsonObject.
   * Example input: String[]{"-key1=value1", "-key2", ...} -> {"key1": "value1", "key2": true}
   *
   * @param args to convert
   * @return jsonObj
   */
  public static JsonObj getArgs(String[] args) {
    if (args == null) {
      return new JsonObj();
    }
    JsonObj valuesMap = new JsonObj(stream(args)
                                        .filter(s -> s.startsWith("-"))
                                        .map(s -> s.replaceFirst("-", "").split("="))
                                        .collect(toMap(s -> s[0], s -> s[1])));
    JsonObj booleanMap = new JsonObj(stream(args)
                                         .filter(s -> s.startsWith("-") && !s.contains("="))
                                         .map(s -> s.replaceFirst("-", ""))
                                         .collect(toMap(s -> s, s -> true)));
    return valuesMap.mergeIn(booleanMap);
  }
}
