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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.h2.tools.SimpleResultSet;
import org.jooq.DataType;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDataType;
import static eu.kyngas.grapes.database.util.Audit.*;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class Jdbc {

  private Jdbc() {
  }

  public static <T> List<T> resultSetToList(ResultSet result, SqlFunction<ResultSet, T> mapper) throws SQLException {
    List<T> list = new ArrayList<>();
    while (result.next()) {
      list.add(mapper.apply(result));
    }
    return list;
  }

  public static Map<String, Object> rowToMap(SimpleResultSet row) throws SQLException {
    Map<String, Object> map = new HashMap<>();
    for (int i = 1; i <= row.getMetaData().getColumnCount(); i++) {
      map.put(row.getColumnName(i), row.getObject(i));
    }
    return map;
  }

  @FunctionalInterface
  public interface SqlFunction<T, S> {
    S apply(T in) throws SQLException;
  }

  public static List<Column> getColumns(Connection conn, String tableName) throws SQLException {
    ResultSet columnResultSet = conn.getMetaData().getColumns(null, SCHEMA, tableName, null);
    Jdbc.SqlFunction<ResultSet, Column> columnMapper = row -> {
      String name = row.getString(4);
      String typeName = row.getString(6);
      int typeLength = row.getInt(7);
      DataType<?> type = DefaultDataType.getDataType(SQLDialect.H2, typeName).length(typeLength);
      return new Column(name, type);
    };
    return Jdbc.resultSetToList(columnResultSet, columnMapper);
  }
}
