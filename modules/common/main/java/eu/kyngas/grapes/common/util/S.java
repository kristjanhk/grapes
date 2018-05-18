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

package eu.kyngas.grapes.common.util;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class S {

  public static String format(String format, Object... args) {
    return String.format(format, args);
  }

  public static <T> String join(Stream<T> stream, String delimiter) {
    return stream.map(Object::toString).collect(Collectors.joining(delimiter));
  }

  public static <T> String join(Collection<T> items) {
    return mapToStrings(items).collect(Collectors.joining(", "));
  }

  public static <T> String join(Collection<T> items, String delimiter) {
    return mapToStrings(items).collect(Collectors.joining(delimiter));
  }

  public static <T> String join(Collection<T> items, String delimiter, String prefix) {
    return mapToStrings(items).collect(Collectors.joining(delimiter, prefix, ""));
  }

  public static <T> String join(Collection<T> items, String delimiter, String prefix, String suffix) {
    return mapToStrings(items).collect(Collectors.joining(delimiter, prefix, suffix));
  }

  public static <T, S> String join(Collection<T> items, Function<T, S> mapper) {
    return Streams.safe(items).map(mapper).map(Object::toString).collect(Collectors.joining(", "));
  }

  private static <T> Stream<String> mapToStrings(Collection<T> items) {
    return Streams.safe(items).map(Object::toString);
  }

  public static String toCamelCase(String snakeCased) {
    String[] parts = snakeCased.split("_");
    if (parts.length == 1) {
      return snakeCased;
    }
    String result = Arrays.stream(parts)
        .filter(s -> s.length() > 0)
        .map(String::toLowerCase)
        .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
        .collect(Collectors.joining());
    return result.substring(0, 1).toLowerCase() + result.substring(1);
  }

  public static String toSnakeCase(String camelCased) {
    return camelCased.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase(Locale.ENGLISH);
  }

  public static String base64(String input) {
    return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
  }

  public static String base64(String format, Object... params) {
    return base64(String.format(format, params));
  }

  public static String toString(Object obj) {
    return ReflectionToStringBuilder.reflectionToString(obj, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  private static String toFieldsString(Object obj) {
    return toFieldsString(obj, 0);
  }

  private static String toFieldsString(Object obj, int depth) {
    if (obj == null) {
      return "null";
    }
    if (obj instanceof String) {
      return "\"" + obj.toString() + "\"";
    }
    if (obj.getClass().isPrimitive() || depth < 0) { // TODO: 19.05.2018 is wrapped primitive
      return obj.toString();
    }
    String name = obj.getClass().getSimpleName();
    StringBuilder sb = new StringBuilder();

    if (obj.getClass().isArray()) {
      Arrays.stream((Object[]) obj).forEach(o -> sb.append(toFieldsString(o, depth - 1)));
      return "[" + sb.toString() + "]";
    }
    if (obj instanceof Iterable) {
      Unsafe.<Iterable<Object>>cast(obj).forEach(o -> sb.append(toFieldsString(o, depth - 1)));
      return "[" + sb.toString() + "]";
    }

    try {
      Field[] fields = obj.getClass().getDeclaredFields();
      for (Field field : fields) {
        field.setAccessible(true);
        sb.append(field.getName()).append("=").append(toFieldsString(field.get(obj), depth - 1)).append(";");
      }
      return name + "[" + (sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "") + "]";
    } catch (IllegalAccessException e) {
      return "IllegalAccess";
    }
  }
}
