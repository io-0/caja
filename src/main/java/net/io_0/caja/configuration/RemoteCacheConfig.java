package net.io_0.caja.configuration;

import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class RemoteCacheConfig extends CacheConfig {
  private String host;

  public RemoteCacheConfig() {
    this.host = "redis://localhost:6379/0";
  }

  public RemoteCacheConfig setHost(String host) {
    this.host = host;
    return this;
  }

  @Override
  public RemoteCacheConfig setTtlInSeconds(Integer ttlInSeconds) {
    return (RemoteCacheConfig) super.setTtlInSeconds(ttlInSeconds);
  }
}
