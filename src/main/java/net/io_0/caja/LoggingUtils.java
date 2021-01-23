package net.io_0.caja;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;

import static lombok.AccessLevel.PRIVATE;
import static net.io_0.caja.configuration.CacheConfig.LogLevel;

@NoArgsConstructor(access = PRIVATE)
public class LoggingUtils {
  public static void logThrough(Logger log, LogLevel logLevel, String format, Object... arguments) {
    switch (logLevel) {
      case TRACE: log.trace(format, arguments); break;
      case INFO: log.info(format, arguments); break;
      case WARN: log.warn(format, arguments); break;
      case ERROR: log.error(format, arguments); break;
      case OFF: break;
      default /* DEBUG */: log.debug(format, arguments);
    }
  }
}
