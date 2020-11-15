package net.io_0.caja.redis;

import io.lettuce.core.api.sync.RedisCommands;
import lombok.NoArgsConstructor;
import net.io_0.caja.sync.Cache;
import java.util.List;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class RedisSyncWrapper {
  public static <K, V> Cache<K, V> wrap(RedisCommands<KeyOrWildcard<K>, V> cache, Integer ttlInSeconds) {
    return new Cache<>() {
      @Override
      public V get(K key) {
        requireNonNull(key);
        return cache.get(KeyOrWildcard.key(key));
      }

      @Override
      public void put(K key, V value) {
        requireNonNull(key);
        cache.setex(KeyOrWildcard.key(key), ttlInSeconds, value);
      }

      @Override @SuppressWarnings("unchecked")
      public boolean containsKey(K key) {
        requireNonNull(key);
        return cache.exists(KeyOrWildcard.key(key)) > 0;
      }

      @Override
      public List<K> keys() {
        return fetchKeys().stream().map(KeyOrWildcard::getKey).collect(toList());
      }

      private List<KeyOrWildcard<K>> fetchKeys() {
        return cache.keys(KeyOrWildcard.wildcard());
      }

      @Override @SuppressWarnings("unchecked")
      public void remove(K key) {
        requireNonNull(key);
        cache.del(KeyOrWildcard.key(key));
      }

      @SuppressWarnings("unchecked")
      @Override
      public void clear() {
        fetchKeys().forEach(cache::del);
      }
    };
  }
}
