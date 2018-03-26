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

package eu.kyngas.grapes.database.entity;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public enum SqlDefault {
  VARCHAR("{null}", String.class),
  NUMBER(-1, Number.class, Integer.class, Long.class, Double.class),
  BOOLEAN(false, Boolean.class),
  DATE(new Date(0), Date.class, Timestamp.class);

  private final Object defaultValue;
  private final List<Class> types;

  SqlDefault(Object defaultValue, Class... types) {
    this.defaultValue = defaultValue;
    this.types = Arrays.asList(types);
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public List<Class> getTypes() {
    return types;
  }

  public static Object getDefaultValue(Class type) {
    return Arrays.stream(values())
        .filter(def -> def.getTypes().stream().anyMatch(defType -> defType.equals(type)))
        .findFirst()
        .map(SqlDefault::getDefaultValue)
        .orElse(null);

  }
}
