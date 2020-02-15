package net.io_0.caja;

import lombok.Getter;
import lombok.ToString;
import java.util.Optional;

@Getter @ToString
public class CacheConfig {
  private Integer ttlInSeconds;
  private Integer heap;
  private Optional<String> host;

  public CacheConfig() {
    this.ttlInSeconds = 1;
    this.heap = 100;
    this.host = Optional.empty();
  }

  public CacheConfig setTtlInSeconds(Integer ttlInSeconds) {
    this.ttlInSeconds = ttlInSeconds;
    return this;
  }

  public CacheConfig setHeap(Integer heap) {
    this.heap = heap;
    return this;
  }

  public CacheConfig setHost(String host) {
    this.host = Optional.ofNullable(host);
    return this;
  }
}
