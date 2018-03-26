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

package eu.kyngas.grapes.common.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import java.util.Arrays;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Getter
public class Logs {
  private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(Logs.class);

  /**
   * Changes vertx logging to SLF4J.
   */
  public static void init(String moduleName) {
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    info(2, "Starting module {}.", moduleName);
  }

  public static void info(String msg, Object... params) {
    LOGGER.callAppenders(createLoggingEvent(Level.INFO, 1, msg, params));
  }

  public static void info(int skip, String msg, Object... params) {
    LOGGER.callAppenders(createLoggingEvent(Level.INFO, skip, msg, params));
  }

  public static void debug(String msg, Object... params) {
    LOGGER.callAppenders(createLoggingEvent(Level.DEBUG, 1, msg, params));
  }

  public static void debug(int skip, String msg, Object... params) {
    LOGGER.callAppenders(createLoggingEvent(Level.DEBUG, skip, msg, params));
  }

  public static void error(String msg, Object... params) {
    LOGGER.callAppenders(createLoggingEvent(Level.ERROR, 1, msg, params));
  }

  public static void error(int skip, String msg, Object... params) {
    LOGGER.callAppenders(createLoggingEvent(Level.ERROR, skip, msg, params));
  }

  private static LoggingEvent createLoggingEvent(Level level, int skip, String msg, Object... params) {
    if (skip < 0) {
      skip = 1;
    }
    FormattingTuple tuple = MessageFormatter.arrayFormat(msg, params);
    boolean hasEx = tuple.getThrowable() != null;

    String message = tuple.getMessage() != null
        ? tuple.getMessage()
        : hasEx
            ? tuple.getThrowable().getMessage()
            : "Unknown cause";

    Throwable def = new Throwable("Unknown cause");
    Throwable throwable = hasEx ? tuple.getThrowable() : def;
    throwable.setStackTrace(Arrays.stream(throwable.getStackTrace())
                                .skip(hasEx ? 0 : skip + 1)
                                .limit(8)
                                .toArray(StackTraceElement[]::new));
    LoggingEvent loggingEvent = new LoggingEvent(Logger.FQCN,
                                                 LOGGER,
                                                 level,
                                                 message,
                                                 Eq.ne(throwable, def) && Eq.eq(level, Level.ERROR, Level.DEBUG)
                                                     ? throwable
                                                     : null,
                                                 null);
    loggingEvent.setCallerData(throwable.getStackTrace());
    return loggingEvent;
  }
}
