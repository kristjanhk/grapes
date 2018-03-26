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
import eu.kyngas.grapes.database.util.Audit;
import eu.kyngas.grapes.database.util.Jdbc;
import eu.kyngas.grapes.database.util.Jooq;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.jooq.CreateTableColumnStep;
import org.jooq.CreateTableConstraintStep;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.conf.ParamType;
import org.jooq.impl.SQLDataType;
import static eu.kyngas.grapes.database.util.Audit.*;
import static org.jooq.impl.DSL.*;


/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@SuppressWarnings("unused")
public class AuditTableCreator {
  private static final String INTEGER = "INTEGER";
  private static final String AUTO_INC = "AUTO_INCREMENT";

  public static void createAuditTable(Connection conn, String tableName) throws SQLException {
    DSLContext dsl = Jooq.getDsl(conn);
    tableName = tableName.toUpperCase(Locale.ENGLISH);
    createTable(conn, dsl, tableName);
    createSysFields(dsl, tableName);
  }

  private static void createTable(Connection conn, DSLContext dsl, String tableName) throws SQLException {
    List<Column> columns = Jdbc.getColumns(conn, tableName);
    Audit.checkIllegalColumnNames(columns, ID_A, SYS_VERSION, SYS_DELETED);
    String auditTableName = Audit.getAuditTableName(tableName);
    CreateTableColumnStep tableStep = dsl.createTable(auditTableName)
        .column(field(ID_A, Integer.class), SQLDataType.INTEGER.nullable(false))
        .column(field(SYS_VERSION, Integer.class), SQLDataType.INTEGER.nullable(false).defaultValue(0))
        .column(field(SYS_DELETED, Boolean.class), SQLDataType.BOOLEAN.nullable(false).defaultValue(false));
    columns.stream()
        .map(column -> field(name(column.getName()), ((DataType<?>) column.getType())))
        .forEach(tableStep::column);
    CreateTableConstraintStep constraintStep =
        tableStep.constraint(constraint(PK_PREFIX + auditTableName).primaryKey(ID_A));
    dsl.execute(addPrimaryKeyAutoIncrement(constraintStep.getSQL(ParamType.INLINED)));

    //language=SQL
    dsl.execute(sql("CREATE TRIGGER " + tableName + "_AUDIT" +
                    " AFTER INSERT, UPDATE, DELETE ON " + tableName +
                    " FOR EACH ROW CALL \"eu.kyngas.grapes.database.trigger.AuditTableUpdater\""));
  }

  private static String addPrimaryKeyAutoIncrement(String sql) {
    String pattern = String.format("%s %s ", ID_A, INTEGER);
    return sql.replaceFirst(pattern, pattern + AUTO_INC + " ");
  }

  private static void createSysFields(DSLContext dsl, String tableName) {
    String alterSysTimeSql = dsl.alterTable(table(name(tableName)))
        .addColumn(field(SYS_TIME, LocalDateTime.class), SQLDataType.LOCALDATETIME.nullable(false))
        .getSQL();
    dsl.execute(alterSysTimeSql + " AS NOW()");
    dsl.alterTable(table(name(tableName)))
        .addColumn(field(SYS_USER, Long.class), SQLDataType.BIGINT.defaultValue(-1L).nullable(false))
        .execute();
  }
}
