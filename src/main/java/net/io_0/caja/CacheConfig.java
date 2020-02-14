package net.io_0.caja;

import lombok.Getter;

@Getter
public class CacheConfig {
  private Integer ttlInSeconds;
  private Integer heap;

  public CacheConfig() {
    this.ttlInSeconds = 1;
    this.heap = 100;
  }

  public CacheConfig setTtlInSeconds(Integer ttlInSeconds) {
    this.ttlInSeconds = ttlInSeconds;
    return this;
  }

  public CacheConfig setHeap(Integer heap) {
    this.heap = heap;
    return this;
  }
}
