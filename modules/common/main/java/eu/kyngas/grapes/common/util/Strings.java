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

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class Strings {

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
}
