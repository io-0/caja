package net.io_0.caja;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import java.time.Duration;

import static java.util.Objects.isNull;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.ResourcePoolsBuilder.heap;

@Slf4j
public class CacheManager {
  private final CacheManagerConfig config;
  private final org.ehcache.CacheManager localManager;

  public CacheManager() {
    this(new CacheConfig());
  }

  public CacheManager(CacheConfig defaultConfig) {
    this(new CacheManagerConfig().setDefaultCacheConfiguration(defaultConfig));
  }

  public CacheManager(CacheManagerConfig config) {
    this.config = config;
    this.localManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
  }

  /**
   * Get or create a cache with the given name.
   *
   * @param name the name of the cache
   * @param keyType the key type the cache should use
   * @param valueType the value type the cache should use
   * @param <K> keyType
   * @param <V> valueType
   * @return created or gathered cache
   *
   * @throws IllegalArgumentException if a cache under that name exist wit different types
   */
  public <K, V> Cache<K, V> get(String name, Class<K> keyType, Class<V> valueType) {
    return get(name, keyType, valueType, config.getDefaultCacheConfiguration());
  }

  /**
   * Get or create a cache with the given name.
   *
   * @param name the name of the cache
   * @param keyType the key type the cache should use
   * @param valueType the value type the cache should use
   * @param <K> keyType
   * @param <V> valueType
   * @param defaultConfig default configuration overwrite, won't be used if specific (named) configuration exists
   * @return created or gathered cache
   *
   * @throws IllegalArgumentException if a cache under that name exist wit different types
   */
  public <K, V> Cache<K, V> get(String name, Class<K> keyType, Class<V> valueType, CacheConfig defaultConfig) {
    org.ehcache.Cache<K, V> localCache = localManager.getCache(name, keyType, valueType);

    if (isNull(localCache)) {
      CacheConfig config = this.config.getCacheConfigurations().getOrDefault(name, defaultConfig);

      localCache = localManager.createCache(
        name,
        newCacheConfigurationBuilder(keyType, valueType, heap(config.getHeap()))
          .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(config.getTtlInSeconds())))
          .build()
      );
      log.trace("{}: created with {}", name, config);
    }
    
    return new LoggingStatisticsCache<>(name, wrap(localCache));
  }

  private <K, V> Cache<K, V> wrap(org.ehcache.Cache<K, V> cache) {
    return new Cache<>() {
      @Override
      public V get(K key) {
        return cache.get(key);
      }

      @Override
      public void put(K key, V value) {
        cache.put(key, value);
      }

      @Override
      public boolean containsKey(K key) {
        return cache.containsKey(key);
      }

      @Override
      public void remove(K key) {
        cache.remove(key);
      }
    };
  }

  public void close() {
    localManager.close();
  }
}
