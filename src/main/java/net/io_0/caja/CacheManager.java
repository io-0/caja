package net.io_0.caja;

import lombok.extern.slf4j.Slf4j;
import net.io_0.caja.ehcache.AsyncWrapper;
import net.io_0.caja.ehcache.SyncWrapper;
import net.io_0.caja.sync.Cache;
import net.io_0.caja.sync.LoggingStatisticsCache;
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
  public <K, V> Cache<K, V> getAsSync(String name, Class<K> keyType, Class<V> valueType) {
    return getAsSync(name, keyType, valueType, config.getDefaultCacheConfiguration());
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
  public <K, V> net.io_0.caja.async.Cache<K, V> getAsAsync(String name, Class<K> keyType, Class<V> valueType) {
    return getAsAsync(name, keyType, valueType, config.getDefaultCacheConfiguration());
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
  public <K, V> Cache<K, V> getAsSync(String name, Class<K> keyType, Class<V> valueType, CacheConfig defaultConfig) {
    return new LoggingStatisticsCache<>(name, SyncWrapper.wrap(getLocalCache(name, keyType, valueType, defaultConfig)));
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
  public <K, V> net.io_0.caja.async.Cache<K, V> getAsAsync(String name, Class<K> keyType, Class<V> valueType, CacheConfig defaultConfig) {
    return new net.io_0.caja.async.LoggingStatisticsCache<>(name, AsyncWrapper.wrap(getLocalCache(name, keyType, valueType, defaultConfig)));
  }

  private <K, V> org.ehcache.Cache<K, V> getLocalCache(String name, Class<K> keyType, Class<V> valueType, CacheConfig defaultConfig) {
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

    return localCache;
  }

  public void close() {
    localManager.close();
  }
}
