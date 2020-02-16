package net.io_0.caja.configuration;

import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.Map;

@Getter @ToString
public class CacheManagerConfig {
  private CacheConfig defaultCacheConfiguration;
  private Map<String, CacheConfig> cacheConfigurations;

  public CacheManagerConfig() {
    this.defaultCacheConfiguration = new LocalCacheConfig();
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
