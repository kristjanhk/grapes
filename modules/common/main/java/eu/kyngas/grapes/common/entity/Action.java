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

package eu.kyngas.grapes.common.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@DataObject(generateConverter = true)
public class Action {
  private String action;
  private JsonObject content;

  public Action(JsonObject json) {
    ActionConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    return JsonObj.wrap(json -> ActionConverter.toJson(this, json));
  }

  public String getAction() {
    return this.action;
  }

  public JsonObject getContent() {
    return this.content;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public void setContent(JsonObject content) {
    this.content = content;
  }

  public String toString() {
    return "Action(action=" + this.getAction() + ", content=" + this.getContent() + ")";
  }
}
