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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Getter
@ToString
@RequiredArgsConstructor(staticName = "of")
public class Pair<T, S> {
  private final T fst;
  private final S snd;

  public static <T, S> PairBuilder<T, S> builder() {
    return new PairBuilder<>();
  }

  public <R> Pair<R, S> mapFst(R fst) {
    return Pair.of(fst, snd);
  }

  public <R> Pair<T, R> mapSnd(R snd) {
    return Pair.of(fst, snd);
  }

  @ToString
  public static class PairBuilder<T, S> {
    private T fst;
    private S snd;

    public PairBuilder<T, S> fst(T fst) {
      this.fst = fst;
      return this;
    }

    public PairBuilder<T, S> snd(S snd) {
      this.snd = snd;
      return this;
    }

    public Pair<T, S> build() {
      return new Pair<>(fst, snd);
    }
  }
}