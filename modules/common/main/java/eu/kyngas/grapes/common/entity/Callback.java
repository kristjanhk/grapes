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

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public interface Callback {

  interface Paramless {
    void exec() throws Exception;
  }

  interface Param<T> {
    void exec(T in) throws Exception;
  }

  interface BiParam<T, S> {
    void exec(T in1, S in2) throws Exception;
  }

  interface Returning<T> {
    T exec() throws Exception;
  }

  interface ReturningParam<T, S> {
    T exec(S in) throws Exception;
  }

  interface ReturningBiParam<T, S, V> {
    T exec(S in1, V in2) throws Exception;
  }
}
