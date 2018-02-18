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

package eu.kyngas.grapes.music.spotify;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.ToString;
import static eu.kyngas.grapes.common.entity.JsonObj.*;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@ToString
@DataObject(generateConverter = true)
public class Token {
  private String accessToken;
  private String tokenType;
  private String scope;
  private int expiresIn;
  private String refreshToken;

  public Token(JsonObject json) {
    TokenConverter.fromJson(toCamelCase(json), this);
  }

  public JsonObject toJson() {
    return toSnakeCase(wrap(json -> TokenConverter.toJson(this, json)));
  }

  public String getAccessToken() {
    return this.accessToken;
  }

  public String getTokenType() {
    return this.tokenType;
  }

  public String getScope() {
    return this.scope;
  }

  public int getExpiresIn() {
    return this.expiresIn;
  }

  public String getRefreshToken() {
    return this.refreshToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public void setExpiresIn(int expiresIn) {
    this.expiresIn = expiresIn;
  }

  public Token setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
    return this;
  }
}
