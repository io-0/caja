package net.io_0.caja.configuration;

import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class LocalCacheConfig extends CacheConfig {
  private Integer heap;

  public LocalCacheConfig() {
    this.heap = 100;
  }

  public LocalCacheConfig setHeap(Integer heap) {
    this.heap = heap;
    return this;
  }

  @Override
  public LocalCacheConfig setTtlInSeconds(Integer ttlInSeconds) {
    return (LocalCacheConfig) super.setTtlInSeconds(ttlInSeconds);
  }
}
