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

import java.util.Locale;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class Rand {
  private static final SplittableRandom RANDOM = new SplittableRandom();

  public static String generateMacAddress() {
    return RANDOM.ints(0, 255)
        .limit(6)
        .mapToObj(Integer::toHexString)
        .map(s -> s.toUpperCase(Locale.ENGLISH))
        .collect(Collectors.joining(":"));
  }
}
