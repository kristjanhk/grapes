package eu.kyngas.grapes.common.util;

import eu.kyngas.grapes.common.entity.JsonObj;
import io.vertx.core.json.JsonObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public class Config {
  private static final String GLOBAL_CONFIG = "/global.json";
  private static final boolean IS_RUNNING_FROM_JAR = getLocation().toString().endsWith(".jar");

  public static JsonObj getGlobal() {
    return getJson(GLOBAL_CONFIG);
  }

  public static JsonObj getJson(String location) {
    String formattedLocation = checkJsonFormat(location);
    try {
      return getDefaultConfig(formattedLocation).mergeIn(new JsonObj(readToString(formattedLocation)));
    } catch (IOException e) {
      log.error("Config {} not found.", formattedLocation);
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

  public static JsonObj getProperties(String location) {
    String formattedLocation = checkPropertiesFormat(location);
    try {
      return new JsonObj(Stream.of(readToString(formattedLocation).split("\n"))
                             .filter(line -> !line.startsWith("#") && line.contains("="))
                             .map(line -> line.split("=", 2))
                             .filter(s -> s.length == 2)
                             .collect(Collectors.toMap(s -> s[0], s -> s[1])));
    } catch (IOException ignored) {
      log.error("Config {} not found.", formattedLocation);
    }
    return new JsonObj();
  }

  private static JsonObj getDefaultConfig(String location) {
    try {
      return new JsonObj(readToString(location.replace(".json", "-default.json")));
    } catch (IOException ignored) {
    }
    return new JsonObj();
  }

  private static String checkJsonFormat(String location) {
    if (location == null) {
      return GLOBAL_CONFIG;
    }
    return checkFormat(location, ".json");
  }

  private static String checkPropertiesFormat(String location) {
    return checkFormat(location, ".properties");
  }

  private static String checkFormat(String location, String suffix) {
    if (!location.startsWith("/")) {
      location = "/" + location;
    }
    if (!suffix.startsWith(".")) {
      suffix = "." + suffix;
    }
    if (!location.endsWith(suffix)) {
      location += suffix;
    }
    return location.toLowerCase(Locale.ENGLISH);
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
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      return S.join(reader.lines(), "\n");
    }
  }

  public static List<String> readFile(String absolutePath) {
    try {
      return Files.readAllLines(Paths.get(absolutePath), StandardCharsets.UTF_8);
    } catch (IOException e) {
      Logs.error("Failed to read file {}", absolutePath, e);
    }
    return Collections.emptyList();
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
                                        .filter(array -> array.length == 2)
                                        .collect(toMap(s -> s[0], s -> s[1])));
    JsonObj booleanMap = new JsonObj(stream(args)
                                         .filter(s -> s.startsWith("-") && !s.contains("="))
                                         .map(s -> s.replaceFirst("-", ""))
                                         .collect(toMap(s -> s, s -> true)));
    return valuesMap.mergeIn(booleanMap);
  }
}
