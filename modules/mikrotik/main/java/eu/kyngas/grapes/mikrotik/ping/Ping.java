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

package eu.kyngas.grapes.mikrotik.ping;

import eu.kyngas.grapes.common.entity.JsonObj;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@ToString
@AllArgsConstructor
@DataObject(generateConverter = true)
public class Ping {
  private String host;
  private int time;
  private boolean success;
  private String errorMessage;
  
  public Ping(JsonObject json) {
    PingConverter.fromJson(json, this);
  }
  
  public JsonObject toJson() {
    return JsonObj.wrap(json -> PingConverter.toJson(this, json));
  }

  public static Ping timedOut(String host) {
    return new Ping(host, -1, false, "Request timed out");
  }

  public static Ping unreachable(String host) {
    return new Ping(host, -1, false, "Destination host unreachable");
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getTime() {
    return time;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
