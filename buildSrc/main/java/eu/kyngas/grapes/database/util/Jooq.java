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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.h2.tools.SimpleResultSet;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.conf.RenderNameStyle;
import org.jooq.impl.DSL;
import static eu.kyngas.grapes.database.util.Audit.ID;
import static eu.kyngas.grapes.database.util.Audit.SYS_DELETED;
import static eu.kyngas.grapes.database.util.Audit.SYS_VERSION;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class Jooq {

  private Jooq() {
  }

  public static DSLContext getDsl(Connection conn) {
    DSLContext dsl = DSL.using(conn);
    dsl.settings().setRenderNameStyle(RenderNameStyle.UPPER);
    return dsl;
  }

  public static void insertMap(DSLContext dsl, String auditTableName, Map<String, Object> map) {
    List<Map.Entry<String, Object>> entries = new ArrayList<>(map.entrySet());
    dsl.insertInto(table(name(auditTableName)))
        .columns(entries.stream()
                     .map(e -> field(name(e.getKey())))
                     .collect(Collectors.toList()))
        .values(entries.stream()
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList()))
        .execute();
  }

  public static void auditDelete(DSLContext dsl, String auditTableName, SimpleResultSet oldRow) throws SQLException {
    Map<String, Object> deletedMap = new HashMap<>();
    deletedMap.put(SYS_DELETED, true);
    deletedMap.put(SYS_VERSION, incrementAndGetSysVersion(dsl, auditTableName, (Long) Jdbc.rowToMap(oldRow).get(ID)));
    insertMap(dsl, auditTableName, deletedMap);
  }

  public static Integer incrementAndGetSysVersion(DSLContext dsl, String auditTableName, long id) {
    Field<Integer> versionField = field(name(SYS_VERSION), Integer.class);
    Integer version = dsl.select(versionField)
        .from(name(auditTableName))
        .where(field(name(ID), Long.class).equal(id))
        .orderBy(versionField.desc())
        .fetchAny(versionField);
    return version == null ? 0 : version + 1;
  }
}
