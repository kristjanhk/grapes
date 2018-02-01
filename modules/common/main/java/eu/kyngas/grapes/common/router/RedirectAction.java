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

package eu.kyngas.grapes.common.router;

import eu.kyngas.grapes.common.entity.Action;
import eu.kyngas.grapes.common.entity.JsonObj;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.ToString;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@ToString
@DataObject(generateConverter = true)
public class RedirectAction implements Action {
  private String url;

  public RedirectAction(String url) {
    this.url = url;
  }

  public RedirectAction(JsonObject json) {
    RedirectActionConverter.fromJson(json, this);
  }

  @Override
  public JsonObject toJson() {
    return JsonObj.wrap(json -> RedirectActionConverter.toJson(this, json));
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void redirect(RoutingContext ctx) {
    ctx.response().putHeader(Header.LOCATION, url).setStatusCode(Status.FOUND).end();
  }
}
