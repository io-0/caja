package net.io_0.caja;

import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.io_0.caja.configuration.CacheConfig;
import net.io_0.caja.configuration.CacheManagerConfig;
import net.io_0.caja.configuration.LocalCacheConfig;
import net.io_0.caja.configuration.RemoteCacheConfig;
import net.io_0.caja.ehcache.EhcacheAsyncWrapper;
import net.io_0.caja.ehcache.EhcacheSyncWrapper;
import net.io_0.caja.redis.JsonObjectCodec;
import net.io_0.caja.redis.KeyOrWildcard;
import net.io_0.caja.redis.RedisAsyncWrapper;
import net.io_0.caja.redis.RedisSyncWrapper;
import net.io_0.caja.sync.Cache;
import net.io_0.caja.sync.LoggingStatisticsDecorator;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;
import static net.io_0.caja.configuration.CacheConfig.LogLevel;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.ResourcePoolsBuilder.heap;

@Slf4j
public class CacheManager {
  private final CacheManagerConfig config;
  private final org.ehcache.CacheManager localManager;

  public CacheManager() {
    this(new LocalCacheConfig());
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
  public <K, V> Cache<K, V> getAsSync(String name, Class<K> keyType, Class<V> valueType, Class<?>... valueSubTypes) {
    return getAsSync(name, Context.ofDefaultConfig(config.getDefaultCacheConfiguration()), keyType, valueType, valueSubTypes);
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
  public <K, V> net.io_0.caja.async.Cache<K, V> getAsAsync(String name, Class<K> keyType, Class<V> valueType, Class<?>... valueSubTypes) {
    return getAsAsync(name, Context.ofDefaultConfig(config.getDefaultCacheConfiguration()), keyType, valueType, valueSubTypes);
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
  public <K, V> Cache<K, V> getAsSync(String name, Context context, Class<K> keyType, Class<V> valueType, Class<?>... valueSubTypes) {
    CacheConfig cfg = this.config.getCacheConfigurations().getOrDefault(name, context.defaultConfig);
    var cache = cfg instanceof LocalCacheConfig
      ? EhcacheSyncWrapper.wrap(getLocalCache(name, keyType, valueType, (LocalCacheConfig) cfg))
      : RedisSyncWrapper.wrap(getSyncRemoteCache(name, (RemoteCacheConfig) cfg, keyType, valueType, valueSubTypes), cfg.getTtlInSeconds());
    if (!LogLevel.OFF.equals(cfg.getLogStatistics())) {
      cache = new LoggingStatisticsDecorator<>(name, cfg.getLogStatistics(), cache);
    }
    return cache;
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
  public <K, V> net.io_0.caja.async.Cache<K, V> getAsAsync(String name, Context context, Class<K> keyType, Class<V> valueType, Class<?>... valueSubTypes) {
    CacheConfig cfg = this.config.getCacheConfigurations().getOrDefault(name, context.defaultConfig);
    var cache = cfg instanceof LocalCacheConfig
      ? EhcacheAsyncWrapper.wrap(getLocalCache(name, keyType, valueType, (LocalCacheConfig) cfg))
      : RedisAsyncWrapper.wrap(getAsyncRemoteCache(name, (RemoteCacheConfig) cfg, keyType, valueType, valueSubTypes), cfg.getTtlInSeconds());
    if (!LogLevel.OFF.equals(cfg.getLogStatistics())) {
      cache = new net.io_0.caja.async.LoggingStatisticsDecorator<>(name, cfg.getLogStatistics(), cache);
    }
    return cache;
  }

  @Builder
  public static class Context {
    @Builder.Default
    // default configuration overwrite, won't be used if specific (named) configuration exists
    private CacheConfig defaultConfig = new LocalCacheConfig();

    public static Context of() {
      return builder().build();
    }

    public static Context ofDefaultConfig(CacheConfig dC) {
      return builder().defaultConfig(dC).build();
    }
  }

  /**
   * @deprecated Use {@link #getAsSync(String, Context, Class, Class, Class...)} instead
   */
  @Deprecated(forRemoval = true)
  public <K, V> Cache<K, V> getAsSync(String name, Class<K> keyType, Class<V> valueType, CacheConfig defaultConfig) {
    return getAsSync(name, Context.ofDefaultConfig(defaultConfig), keyType, valueType);
  }

  /**
   * @deprecated Use {@link #getAsAsync(String, Context, Class, Class, Class...)} instead
   */
  @Deprecated(forRemoval = true)
  public <K, V> net.io_0.caja.async.Cache<K, V> getAsAsync(String name, Class<K> keyType, Class<V> valueType, CacheConfig defaultConfig) {
    return getAsAsync(name, Context.ofDefaultConfig(defaultConfig), keyType, valueType);
  }

  private <K, V> org.ehcache.Cache<K, V> getLocalCache(String name, Class<K> keyType, Class<V> valueType, LocalCacheConfig config) {
    org.ehcache.Cache<K, V> localCache = localManager.getCache(name, keyType, valueType);

    if (isNull(localCache)) {
      localCache = localManager.createCache(name,
        newCacheConfigurationBuilder(keyType, valueType, heap(config.getHeap()))
          .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(config.getTtlInSeconds())))
          .build()
      );
      log.debug("{}: created with {}", name, config);
    }

    return localCache;
  }

  private <K, V> RedisCommands<KeyOrWildcard<K>, V> getSyncRemoteCache(String name, RemoteCacheConfig config, Class<K> keyType, Class<V> valueType, Class<?>... valueSubTypes) {
    return getRemoteCacheConnection(name, config, keyType, valueType, valueSubTypes).sync();
  }

  private <K, V> RedisAsyncCommands<KeyOrWildcard<K>, V> getAsyncRemoteCache(String name, RemoteCacheConfig config, Class<K> keyType, Class<V> valueType, Class<?>... valueSubTypes) {
    return getRemoteCacheConnection(name, config, keyType, valueType, valueSubTypes).async();
  }

  @SuppressWarnings("unchecked")
  private <K, V> StatefulRedisConnection<KeyOrWildcard<K>, V> getRemoteCacheConnection(String name, RemoteCacheConfig config, Class<K> keyType, Class<V> valueType, Class<?>... valueSubTypes) {
    String host = config.getHost();

    RedisClient client = clients.get(host);
    if (isNull(client)) {
      client = RedisClient.create(host);
      clients.put(host, client);
      log.debug("{}: created client with {}", name, config);
    }

    StatefulRedisMasterReplicaConnection<KeyOrWildcard<K>, V> connection = (StatefulRedisMasterReplicaConnection<KeyOrWildcard<K>, V>) connections.get(String.format("%s|%s", name, host));
    if (isNull(connection)) {
      connection = MasterReplica.connect(client, new JsonObjectCodec<>(name, keyType, valueType, valueSubTypes), RedisURI.create(host));
      connections.put(String.format("%s|%s", name, host), connection);
      log.debug("{}: created connection with {}", name, config);
    }

    return connection;
  }

  private Map<String, RedisClient> clients = new ConcurrentHashMap<>();
  private Map<String, StatefulRedisConnection<?, ?>> connections = new ConcurrentHashMap<>();

  @RequiredArgsConstructor
  static class ClientAndConnection<K, V> {
    final RedisClient client;
    final StatefulRedisConnection<K, V> connection;
  }

  public void close() {
    localManager.close();

    connections.values().forEach(StatefulConnection::close);
    clients.values().forEach(AbstractRedisClient::shutdown);
  }
}
