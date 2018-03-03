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

package eu.kyngas.grapes.common.service;

import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.Unsafe;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public abstract class ProxyServiceImpl<T> implements ProxyService {
  protected final ServiceBinder serviceBinder;
  protected final MessageConsumer<JsonObject> messageConsumer;

  protected ProxyServiceImpl(String address, Class<T> serviceClass) {
    this.serviceBinder = new ServiceBinder(Ctx.vertx());
    this.messageConsumer = this.serviceBinder.setAddress(address).register(serviceClass, Unsafe.cast(this));
  }

  @Override
  public void close() {
    serviceBinder.unregister(messageConsumer);
  }
}
