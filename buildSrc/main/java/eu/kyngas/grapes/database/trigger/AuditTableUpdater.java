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

package eu.kyngas.grapes.database.trigger;

import eu.kyngas.grapes.database.entity.SqlDefault;
import eu.kyngas.grapes.database.util.Jdbc;
import eu.kyngas.grapes.database.util.Jooq;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import org.h2.tools.SimpleResultSet;
import org.h2.tools.TriggerAdapter;
import org.jooq.DSLContext;
import static eu.kyngas.grapes.database.util.Audit.*;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@SuppressWarnings("unused")
public class AuditTableUpdater extends TriggerAdapter {

  @Override
  public void fire(Connection conn, ResultSet oldRow, ResultSet newRow) throws SQLException {
    DSLContext dsl = Jooq.getDsl(conn);
    String auditTableName = tableName.toUpperCase(Locale.ENGLISH) + SUFFIX;
    if (newRow == null) {
      Jooq.auditDelete(dsl, auditTableName, (SimpleResultSet) oldRow);
      return;
    }
    Map<String, Object> newMap = Jdbc.rowToMap(((SimpleResultSet) newRow));
    if (oldRow == null) {
      Jooq.insertMap(dsl, auditTableName, newMap);
      return;
    }
    Map<String, Object> oldMap = Jdbc.rowToMap(((SimpleResultSet) oldRow));
    auditModifiedColumns(dsl, auditTableName, oldMap, newMap);
  }

  private void auditModifiedColumns(DSLContext dsl,
                                    String auditTableName,
                                    Map<String, Object> oldMap,
                                    Map<String, Object> newMap) {
    Map<String, Object> diffMap = new HashMap<>();
    diffMap.put(SYS_VERSION, Jooq.incrementAndGetSysVersion(dsl, auditTableName, (Long) newMap.get(ID)));
    diffMap.put(ID, newMap.get(ID));

    Predicate<Map.Entry<String, Object>> isUnprocessed = e -> !diffMap.containsKey(e.getKey());
    Predicate<Map.Entry<String, Object>> isExistsValue = e -> e.getValue() != null;
    Predicate<Map.Entry<String, Object>> isExistsOldValue = e -> oldMap.get(e.getKey()) != null;
    Predicate<Map.Entry<String, Object>> isExistsNewValue = e -> newMap.get(e.getKey()) != null;
    Predicate<Map.Entry<String, Object>> isModified = e -> !e.getValue().equals(oldMap.get(e.getKey()));

    newMap.entrySet().stream()
        .filter(isUnprocessed)
        .filter(isExistsValue)
        .filter(isExistsOldValue.negate())
        .forEach(e -> diffMap.put(e.getKey(), e.getValue()));

    oldMap.entrySet().stream()
        .filter(isUnprocessed)
        .filter(isExistsValue)
        .filter(isExistsNewValue.negate())
        .forEach(e -> diffMap.put(e.getKey(), SqlDefault.getDefaultValue(e.getValue().getClass())));

    newMap.entrySet().stream()
        .filter(isUnprocessed)
        .filter(isExistsOldValue)
        .filter(isModified)
        .forEach(e -> diffMap.put(e.getKey(), e.getValue()));

    Jooq.insertMap(dsl, auditTableName, diffMap);
  }
}
