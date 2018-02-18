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

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class StringsTest {

  @Test
  public void toCamelCase() {
    assertEquals("camelCase", Strings.toCamelCase("camel_case"));
    assertEquals("camelCaseLong", Strings.toCamelCase("camel_case_long"));
    assertEquals("camelCase", Strings.toCamelCase("camel_case_"));
    assertEquals("camelCase", Strings.toCamelCase("_camel_case"));
    assertEquals("camelCase", Strings.toCamelCase("camel__case"));
    assertEquals("camelCase", Strings.toCamelCase("camelCase"));
  }

  @Test
  public void toSnakeCase() {
    assertEquals("snake_case", Strings.toSnakeCase("snakeCase"));
    assertEquals("snake_case_long", Strings.toSnakeCase("snakeCaseLong"));
    assertEquals("snake_case", Strings.toSnakeCase("SnakeCase"));
    assertEquals("snake_case", Strings.toSnakeCase("snake_case"));
  }
}