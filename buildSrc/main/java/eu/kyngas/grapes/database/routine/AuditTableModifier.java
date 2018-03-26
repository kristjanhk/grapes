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

package eu.kyngas.grapes.database.routine;

import eu.kyngas.grapes.database.entity.Column;
import eu.kyngas.grapes.database.util.Jdbc;
import eu.kyngas.grapes.database.util.Jooq;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.DSLContext;
import org.jooq.DataType;
import static eu.kyngas.grapes.database.util.Audit.*;


/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@SuppressWarnings("unused")
public class AuditTableModifier {

  public static void alterAuditTable(Connection conn, String tableName) throws SQLException {
    DSLContext dsl = Jooq.getDsl(conn);
    tableName = tableName.toUpperCase(Locale.ENGLISH);
    String auditTableName = getAuditTableName(tableName);

    Set<String> ignoredColumnNames =
        Stream.of(ID, ID_A, SYS_VERSION, SYS_DELETED, SYS_TIME, SYS_USER).collect(Collectors.toSet());

    Collector<Column, ?, Map<String, DataType>> collector =
        Collectors.toMap(column -> column.getName().toUpperCase(Locale.ENGLISH), Column::getType);
    Predicate<Column> isIgnoredColumn = column -> ignoredColumnNames.contains(column.getName());

    Map<String, DataType> tableColumns = Jdbc.getColumns(conn, tableName).stream()
        .filter(isIgnoredColumn.negate())
        .collect(collector);
    Map<String, DataType> auditTableColumns = Jdbc.getColumns(conn, auditTableName).stream()
        .filter(isIgnoredColumn.negate())
        .collect(collector);

   alterModifiedColumns(dsl, auditTableName, auditTableColumns, tableColumns);
  }

  private static void alterModifiedColumns(DSLContext dsl,
                                           String auditTableName,
                                           Map<String, DataType> oldMap,
                                           Map<String, DataType> newMap) {
    Map<String, DataType> diffMap = new HashMap<>();

    Predicate<Map.Entry<String, DataType>> isUnprocessed = e -> !diffMap.containsKey(e.getKey());
    Predicate<Map.Entry<String, DataType>> isAdded = e -> !oldMap.containsKey(e.getKey());
    Predicate<Map.Entry<String, DataType>> isDeleted = e -> !newMap.containsKey(e.getKey());
    Predicate<Map.Entry<String, DataType>> isModified = e -> !e.getValue().equals(oldMap.get(e.getKey()));
    Consumer<Map.Entry<String, DataType>> appendToDiff = e -> diffMap.put(e.getKey(), e.getValue());

    newMap.entrySet().stream()
        .filter(isUnprocessed)
        .filter(isAdded)
        .peek(appendToDiff)
        .forEach(e -> dsl.alterTable(auditTableName).addColumn(e.getKey(), e.getValue()).execute());

    oldMap.entrySet().stream()
        .filter(isUnprocessed)
        .filter(isDeleted)
        .peek(appendToDiff)
        .forEach(e -> dsl.alterTable(auditTableName).dropColumn(e.getKey()).execute());

    newMap.entrySet().stream()
        .filter(isUnprocessed)
        .filter(isModified)
        .forEach(e -> dsl.alterTable(auditTableName).alterColumn(e.getKey()).set(e.getValue()).execute());
  }
}
