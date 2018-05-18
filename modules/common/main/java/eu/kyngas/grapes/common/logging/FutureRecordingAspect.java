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

package eu.kyngas.grapes.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
@Aspect
public class FutureRecordingAspect {

  public interface RecordingTime {
    Long getStartTime();

    void setStartTime(long startTime);
  }

  public static class RecordingTimeImpl implements RecordingTime {
    private Long startTime;

    @Override
    public Long getStartTime() {
      return startTime;
    }

    @Override
    public void setStartTime(long startTime) {
      this.startTime = startTime;
    }
  }

  @SuppressWarnings("unused")
  @DeclareParents(value = "io.vertx.core.Future+", defaultImpl = RecordingTimeImpl.class)
  private RecordingTime recorder;

  @Pointcut("within(io.vertx.core.Future+)")
  public void isFuture() {}

  @After(value = "isFuture() && call(* setHandler(..)) && this(r)", argNames = "r")
  public void setHandler(RecordingTime r) {
    r.setStartTime(System.currentTimeMillis());
  }

  @Around(value = "isFuture() && call(* complete(..)) && this(r)", argNames = "point,r")
  public Object success(ProceedingJoinPoint point, RecordingTime r) throws Throwable {
    if (r.getStartTime() != null) {
      long time = System.currentTimeMillis() - r.getStartTime();
      if (point.getArgs().length == 0) {
        log.trace("Future succeeded in {}ms.", time);
      } else {
        log.trace("Future succeeded in {}ms with result '{}'.", time, point.getArgs()[0]);
      }
    }
    return point.proceed();
  }

  @Around(value = "isFuture() && call(* fail(..)) && this(r)", argNames = "point,r")
  public Object fail(ProceedingJoinPoint point, RecordingTime r) throws Throwable {
    String time = r.getStartTime() == null ? "?" : String.valueOf(System.currentTimeMillis() - r.getStartTime());
    Object arg = point.getArgs()[0];
    if (arg instanceof Throwable) {
      log.error("Future failed in {}ms.", time, arg);
    } else {
      log.error("Future failed in {}ms with cause '{}'.", time, arg);
    }
    return point.proceed();
  }
}
