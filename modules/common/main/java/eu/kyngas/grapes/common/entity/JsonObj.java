/*
 * Copyright (C) 2018 Kristjan Hendrik Küngas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.kyngas.grapes.common.entity;

import eu.kyngas.grapes.common.util.N;
import eu.kyngas.grapes.common.util.Strings;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import static eu.kyngas.grapes.common.util.N.ifNotExists;

/**
 * Implementation of JsonObject that avoids casting exceptions and attempts to parse values.
 * Invalid values will be returned as null.
 *
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@NoArgsConstructor
public class JsonObj extends JsonObject {
  private static final Map<Class, Function<String, Object>> PARSER_MAP = new HashMap<>();

  static {
    PARSER_MAP.put(Integer.class, input -> safe(() -> Integer.parseInt(input)));
    PARSER_MAP.put(Long.class, input -> safe(() -> Long.parseLong(input)));
    PARSER_MAP.put(Double.class, input -> safe(() -> Double.parseDouble(input)));
    PARSER_MAP.put(Float.class, input -> safe(() -> Float.parseFloat(input)));
    PARSER_MAP.put(JsonObj.class, input -> safe(() -> Json.decodeValue(input, Map.class))); // {"key":"value"}
    PARSER_MAP.put(JsonArray.class, input -> safe(() -> new JsonArray(Json.decodeValue(input, List.class)))); // [1,2,3]
    // TODO: 25.09.2017 boolean, doesn't throw exception?
  }

  public JsonObj(String json) {
    super(json);
  }

  public JsonObj(Map<String, Object> map) {
    super(map);
  }

  public static JsonObj from(JsonObject parent) {
    return parent == null ? null : new JsonObj(parent.getMap());
  }

  public static JsonObj from(Object parent) {
    return parent == null || !(parent instanceof JsonObject) ? null : new JsonObj(((JsonObject) parent).getMap());
  }

  public static JsonObj toCamelCase(JsonObject json) {
    return new JsonObj(json.getMap().entrySet().stream()
                           .collect(Collectors.toMap(e -> Strings.toCamelCase(e.getKey()), Map.Entry::getValue)));
  }

  public static JsonObj toSnakeCase(JsonObject json) {
    return new JsonObj(json.getMap().entrySet().stream()
                           .collect(Collectors.toMap(e -> Strings.toSnakeCase(e.getKey()), Map.Entry::getValue)));
  }

  public static JsonObj wrap(Consumer<JsonObj> consumer) {
    JsonObj json = new JsonObj();
    N.safe(consumer, c -> c.accept(json));
    return json;
  }

  private Object get(String key) {
    Objects.requireNonNull(key);
    return getMap().get(key);
  }

  /**
   * Gets value as String.
   */
  @Override
  public String getString(String key) {
    Object value = get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof CharSequence) {
      return value.toString();
    }
    return String.valueOf(value);
  }

  /**
   * Gets value as Integer.
   * Parses Integer, Number, String and Boolean types.
   */
  @Override
  public Integer getInteger(String key) {
    Object value = get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof Integer) {
      return ((Integer) value);
    }
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    if (value instanceof String) {
      try {
        return Integer.parseInt(((String) value));
      } catch (NumberFormatException ignored) {
      }
    }
    if (value instanceof Boolean) {
      return (Boolean) value ? 1 : 0;
    }
    return null;
  }

  /**
   * Gets value as Integer.
   * Parses Integer, Number, String and Boolean types.
   */
  @Override
  public Long getLong(String key) {
    Object value = get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof Long) {
      return ((Long) value);
    }
    if (value instanceof Number) {
      return ((Number) value).longValue();
    }
    if (value instanceof String) {
      try {
        return Long.parseLong(((String) value));
      } catch (NumberFormatException ignored) {
      }
    }
    if (value instanceof Boolean) {
      return (Boolean) value ? 1L : 0L;
    }
    return null;
  }

  /**
   * Gets value as Double.
   * Parses Integer, Number, String and Boolean types.
   */
  @Override
  public Double getDouble(String key) {
    Object value = get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof Double) {
      return ((Double) value);
    }
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    if (value instanceof String) {
      try {
        return Double.parseDouble(((String) value));
      } catch (NumberFormatException ignored) {
      }
    }
    if (value instanceof Boolean) {
      return (Boolean) value ? 1d : 0d;
    }
    return null;
  }

  /**
   * Gets value as Float.
   * Parses Integer, Number, String and Boolean types.
   */
  @Override
  public Float getFloat(String key) {
    Object value = get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof Float) {
      return ((Float) value);
    }
    if (value instanceof Number) {
      return ((Number) value).floatValue();
    }
    if (value instanceof String) {
      try {
        return Float.parseFloat(((String) value));
      } catch (NumberFormatException ignored) {
      }
    }
    if (value instanceof Boolean) {
      return (Boolean) value ? 1f : 0f;
    }
    return null;
  }

  /**
   * Gets value as Boolean.
   * True if value is true, "true" or number >= 1.
   */
  @Override
  public Boolean getBoolean(String key) {
    Object value = get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof Boolean) {
      return (Boolean) value;
    } else if (value instanceof Number) {
      return ((Number) value).intValue() >= 1;
    }
    return Boolean.valueOf(String.valueOf(value));
  }

  /**
   * Gets value as JsonObject.
   * JsonObjects are converted to JsonObjs.
   * Parses JsonObject and Map types.
   */
  @SuppressWarnings("unchecked")
  @Override
  public JsonObject getJsonObject(String key) {
    Object value = get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof JsonObject) {
      if (value instanceof JsonObj) {
        return ((JsonObj) value);
      }
      return new JsonObj(((JsonObject) value).getMap());
    }
    if (value instanceof Map) {
      return new JsonObj(((Map<String, Object>) value));
    }
    return null;
  }

  /**
   * Gets value as JsonArray.
   * Parses JsonArray and List types.
   */
  @Override
  public JsonArray getJsonArray(String key) {
    Object value = get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof JsonArray) {
      return ((JsonArray) value);
    }
    if (value instanceof List) {
      return new JsonArray(((List) value));
    }
    return null;
  }

  /**
   * Gets value as byte[].
   */
  @Override
  public byte[] getBinary(String key) {
    Object value = get(key);
    if (value == null) {
      return null;
    }
    try {
      return Base64.getDecoder().decode(String.valueOf(value));
    } catch (IllegalArgumentException ignored) {
    }
    return null;
  }

  @Override
  public String getString(String key, String def) {
    return get(key) != null ? getString(key) : def;
  }

  @Override
  public Integer getInteger(String key, Integer def) {
    return get(key) != null ? getInteger(key) : def;
  }

  @Override
  public Long getLong(String key, Long def) {
    return get(key) != null ? getLong(key) : def;
  }

  @Override
  public Double getDouble(String key, Double def) {
    return get(key) != null ? getDouble(key) : def;
  }

  @Override
  public Float getFloat(String key, Float def) {
    return get(key) != null ? getFloat(key) : def;
  }

  @Override
  public Boolean getBoolean(String key, Boolean def) {
    return get(key) != null ? getBoolean(key) : def;
  }

  @Override
  public JsonObject getJsonObject(String key, JsonObject def) {
    return get(key) != null ? getJsonObject(key) : def;
  }

  @Override
  public JsonArray getJsonArray(String key, JsonArray def) {
    return get(key) != null ? getJsonArray(key) : def;
  }

  @Override
  public byte[] getBinary(String key, byte[] def) {
    return get(key) != null ? getBinary(key) : def;
  }

  public JsonObj putIfAbsent(String key, Object value) {
    getMap().putIfAbsent(key, value);
    return this;
  }

  public JsonObj putParse(String key, Object value) {
    if (key == null) {
      return this;
    } else if (value == null) {
      putNull(key);
      return this;
    }
    Object parsedValue;
    String stringValue = value.toString();
    parsedValue = PARSER_MAP.get(Integer.class).apply(stringValue);
    parsedValue = ifNotExists(parsedValue, () -> PARSER_MAP.get(Long.class).apply(stringValue));
    parsedValue = ifNotExists(parsedValue, () -> PARSER_MAP.get(Float.class).apply(stringValue));
    parsedValue = ifNotExists(parsedValue, () -> PARSER_MAP.get(Double.class).apply(stringValue));
    parsedValue = ifNotExists(parsedValue, () -> PARSER_MAP.get(JsonObj.class).apply(stringValue));
    parsedValue = ifNotExists(parsedValue, () -> PARSER_MAP.get(JsonArray.class).apply(stringValue));
    // TODO: 25.09.2017 boolean
    parsedValue = ifNotExists(parsedValue, () -> stringValue);
    put(key, parsedValue);
    return this;
  }

  private static Object safe(Supplier<Object> parseFunction) {
    try {
      return parseFunction.get();
    } catch (Exception ignored) {
    }
    return null;
  }

  public JsonObj mergeIn(JsonObj other) {
    return (JsonObj) super.mergeIn(other);
  }
}
