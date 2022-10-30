package net.io_0.caja.configuration;

import lombok.Getter;
import lombok.ToString;

import static net.io_0.caja.configuration.RemoteCacheConfig.ReadFrom.*;

@Getter @ToString
public class RemoteCacheConfig extends CacheConfig {
  private String host;
  private ReadFrom readFrom = UPSTREAM;

  public RemoteCacheConfig() {
    this.host = "redis://localhost:6379/0";
  }

  public RemoteCacheConfig setHost(String host) {
    this.host = host;
    return this;
  }

  public RemoteCacheConfig setReadFrom(ReadFrom readFrom) {
    this.readFrom = readFrom;
    return this;
  }

  @Override
  public RemoteCacheConfig setTtlInSeconds(Integer ttlInSeconds) {
    return (RemoteCacheConfig) super.setTtlInSeconds(ttlInSeconds);
  }

  public enum ReadFrom {
    UPSTREAM, UPSTREAM_PREFERRED, REPLICA, REPLICA_PREFERRED
  }
}
