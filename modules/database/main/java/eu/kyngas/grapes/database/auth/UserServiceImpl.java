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

package eu.kyngas.grapes.database.auth;

import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.database.dao.DaoContext;
import eu.kyngas.grapes.database.tables.daos.UserDao;
import eu.kyngas.grapes.database.tables.interfaces.IUser;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import java.util.List;
import org.jooq.Configuration;
import static eu.kyngas.grapes.common.util.Streams.mapList;
import static eu.kyngas.grapes.database.tables.User.USER;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class UserServiceImpl extends DaoContext implements UserService {
  private final UserDao userDao;

  public UserServiceImpl(Configuration jooq) {
    super(jooq);
    this.userDao = new UserDao(jooq, Ctx.vertx());
  }

  @Override
  public UserService findAllUsers(Handler<AsyncResult<List<JsonObject>>> handler) {
    handler.handle(userDao.findAll().map(list -> mapList(list, IUser::toJson)));
    return this;
  }

  @Override
  public UserService findUsersByName(String name, Handler<AsyncResult<List<JsonObject>>> handler) {
    handler.handle(userDao.findManyByCondition(USER.USERNAME.eq(name)).map(users -> mapList(users, IUser::toJson)));
    return this;
  }
}

