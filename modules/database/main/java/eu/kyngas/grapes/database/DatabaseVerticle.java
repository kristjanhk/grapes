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

package eu.kyngas.grapes.database;

import eu.kyngas.grapes.common.entity.JsonObj;
import eu.kyngas.grapes.common.entity.Verticle;
import eu.kyngas.grapes.common.util.Config;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.H;
import eu.kyngas.grapes.common.util.Logs;
import eu.kyngas.grapes.database.dao.DatabaseManager;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class DatabaseVerticle extends Verticle {
  private static final String MODULE_NAME = "Database";
  private DatabaseManager databaseManager;

  public static void main(String[] args) {
    Logs.init(MODULE_NAME);
    JsonObj config = Config.getGlobal()
        .deepMergeIn(Config.getProperties(MODULE_NAME))
        .deepMergeIn(Config.getProperties("database-secret"))
        .deepMergeIn(Config.getArgs(args));
    Ctx.create(vertx -> vertx.deployVerticle(new DatabaseVerticle(),
                                             new DeploymentOptions().setConfig(config),
                                             ar -> H.handleVerticleStarted(vertx, MODULE_NAME).handle(ar)));
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    databaseManager = new DatabaseManager().start(startFuture);
    //todo main router -> sockjs router -> daos

    //todo transactions, tests, starting h2 as test db
  }

  @Override
  public void start() throws Exception {

  }

  @Override
  public void stop() {
    databaseManager.stop();
  }
}
