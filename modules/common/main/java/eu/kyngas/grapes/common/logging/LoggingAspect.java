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

import eu.kyngas.grapes.common.util.S;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
@Aspect
public class LoggingAspect {

  @Around("(@within(Loggable) || @annotation(Loggable)) && call(* *(..))")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    String methodName = ((MethodSignature) point.getSignature()).getMethod().getName();
    Object[] args = point.getArgs();
    Object result = point.proceed();

    log.trace("Called {}() with args {} returning {}.", methodName, S.toString(args), S.toString(result));
    return result;
  }


}
