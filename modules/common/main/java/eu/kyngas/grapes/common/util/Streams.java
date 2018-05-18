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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class Streams {

  public static <T, S> List<S> mapList(Collection<T> items, Function<T, S> mapper) {
    return stream(items).map(mapper).collect(Collectors.toList());
  }

  public static <T> List<T> filterList(Collection<T> items, Predicate<T> filter) {
    return stream(items).filter(filter).collect(Collectors.toList());
  }

  public static <T, S> Map<S, T> collectMap(Collection<T> items, Function<T, S> keyMapper) {
    return stream(items).collect(Collectors.toMap(keyMapper, Function.identity()));
  }

  public static <T, S, V> Map<S, V> collectMap(Collection<T> items,
                                               Function<T, S> keyMapper,
                                               Function<T, V> valueMapper) {
    return stream(items).collect(Collectors.toMap(keyMapper, valueMapper));
  }

  public static <T> Stream<T> stream(Collection<T> items) {
    return items == null ? Stream.empty() : items.stream();
  }

  public static <T> Stream<T> safe(Collection<T> items) {
    return items == null ? Stream.empty() : items.stream().filter(Objects::nonNull);
  }

  public static <T> T findFirst(Collection<T> items, Predicate<T> filter) {
    return safe(items).filter(filter).findFirst().orElse(null);
  }

  public static <T, S> Map<T, S> zipToMap(Collection<T> keyList, Collection<S> valueList) {
    if (keyList.size() != valueList.size()) {
      Logs.warn("KeyList size mismatches valueList size, excess values will be discarded.");
    }
    Map<T, S> map = new HashMap<>();
    Iterator<S> valueIt = valueList.iterator();
    keyList.iterator().forEachRemaining(key -> C.ifTrue(valueIt.hasNext(), () -> map.put(key, valueIt.next())));
    return map;
  }
}
