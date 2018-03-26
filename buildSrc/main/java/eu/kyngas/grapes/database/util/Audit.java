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

package eu.kyngas.grapes.database.util;

import eu.kyngas.grapes.database.entity.Column;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class Audit {
  public static final String SCHEMA = "GRAPES";
  public static final String SUFFIX = "_A";
  public static final String ID = "ID";
  public static final String ID_A = "ID_A";
  public static final String SYS_VERSION = "SYS_VERSION";
  public static final String SYS_DELETED = "SYS_DELETED";
  public static final String PK_PREFIX = "PK_";

  private Audit() {
  }

  public static void checkIllegalColumnNames(List<Column> columns, String... names) {
    columns.forEach(column -> Arrays.stream(names).filter(name -> name.equals(column.getName())).forEach(name -> {
      throw new IllegalArgumentException(String.format("Regular table cannot contain column with name '%s'", name));
    }));
  }
}
