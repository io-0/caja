package net.io_0.caja.configuration;

import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public abstract class CacheConfig {
  private Integer ttlInSeconds;
  private LogLevel logStatistics;

  public CacheConfig() {
    this.ttlInSeconds = 1;
    this.logStatistics = LogLevel.DEBUG;
  }

  public CacheConfig setTtlInSeconds(Integer ttlInSeconds) {
    this.ttlInSeconds = ttlInSeconds;
    return this;
  }

  public CacheConfig setLogStatistics(LogLevel logStatistics) {
    this.logStatistics = logStatistics;
    return this;
  }

  public enum LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, OFF
  }
}
