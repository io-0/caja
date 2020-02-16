package net.io_0.caja.configuration;

import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public abstract class CacheConfig {
  private Integer ttlInSeconds;

  public CacheConfig() {
    this.ttlInSeconds = 1;
  }

  public CacheConfig setTtlInSeconds(Integer ttlInSeconds) {
    this.ttlInSeconds = ttlInSeconds;
    return this;
  }
}
