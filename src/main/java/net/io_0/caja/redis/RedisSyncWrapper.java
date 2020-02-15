package net.io_0.caja.redis;

import io.lettuce.core.api.sync.RedisCommands;
import net.io_0.caja.sync.Cache;

public class RedisSyncWrapper {
  public static <K, V> Cache<K, V> wrap(RedisCommands<K, V> cache, Integer ttlInSeconds) {
    return new Cache<>() {
      @Override
      public V get(K key) {
        return cache.get(key);
      }

      @Override
      public void put(K key, V value) {
        cache.setex(key, ttlInSeconds, value);
      }

      @Override @SuppressWarnings("unchecked")
      public boolean containsKey(K key) {
        return cache.exists(key) > 0;
      }

      @Override @SuppressWarnings("unchecked")
      public void remove(K key) {
        cache.del(key);
      }
    };
  }
}
