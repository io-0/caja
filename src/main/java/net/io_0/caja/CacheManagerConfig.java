package net.io_0.caja;

import lombok.Getter;
import java.util.Collections;
import java.util.Map;

@Getter
public class CacheManagerConfig {
  private CacheConfig defaultCacheConfiguration;
  private Map<String, CacheConfig> cacheConfigurations;

  public CacheManagerConfig() {
    this.defaultCacheConfiguration = new CacheConfig();
    this.cacheConfigurations = Collections.emptyMap();
  }

  public CacheManagerConfig setDefaultCacheConfiguration(CacheConfig defaultCacheConfiguration) {
    this.defaultCacheConfiguration = defaultCacheConfiguration;
    return this;
  }

  public CacheManagerConfig setCacheConfigurations(Map<String, CacheConfig> cacheConfigurations) {
    this.cacheConfigurations = cacheConfigurations;
    return this;
  }
}
