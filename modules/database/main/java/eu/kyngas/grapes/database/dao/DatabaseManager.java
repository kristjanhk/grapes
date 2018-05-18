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

package eu.kyngas.grapes.database.dao;

import eu.kyngas.grapes.common.util.Ctx;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.sql.SQLException;
import lombok.Getter;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class DatabaseManager {
  private final JsonObject config = Ctx.config();
  private final Server server = Server.createTcpServer();
  private final JdbcDataSource dataSource = createDataSource();
  @Getter
  private final Configuration jooqConfiguration = createJooqConfiguration();

  public DatabaseManager() throws SQLException {
  }

  public DatabaseManager start(Future<Void> future) throws SQLException {
     server.start();
     runMigration(future);
     return this;
  }

  public void stop() {
    server.shutdown();
  }

  private JdbcDataSource createDataSource() {
    JdbcDataSource jdbcDataSource = new JdbcDataSource();
    jdbcDataSource.setURL(config.getString("url"));
    jdbcDataSource.setUser(config.getString("user"));
    jdbcDataSource.setPassword(config.getString("password"));
    return jdbcDataSource;
  }

  private Configuration createJooqConfiguration() {
    Configuration configuration = new DefaultConfiguration();
    configuration.set(SQLDialect.H2);
    configuration.set(dataSource);
    return configuration;
  }

  private void runMigration(Future<Void> future) {
    future.setHandler(Ctx.blocking(() -> {
      Flyway flyway = new Flyway();
      flyway.setDataSource(dataSource);
      flyway.setSchemas(config.getString("schema"));
      flyway.setLocations(config.getString("location_production"));
      flyway.setOutOfOrder(config.getBoolean("out_of_order"));
      flyway.setSqlMigrationPrefix(config.getString("prefix"));
      flyway.setSqlMigrationSeparator(config.getString("separator"));
      flyway.migrate();
    }));

  }
}
