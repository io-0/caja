package net.io_0.caja.ehcache;

import lombok.NoArgsConstructor;
import net.io_0.caja.sync.Cache;
import java.util.List;

import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class EhcacheSyncWrapper {
  public static <K, V> Cache<K, V> wrap(org.ehcache.Cache<K, V> cache) {
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
      public List<K> keys() {
        return stream(cache.spliterator(), false).map(org.ehcache.Cache.Entry::getKey).collect(toList());
      }

      @Override
      public void remove(K key) {
        cache.remove(key);
      }

      @Override
      public void clear() {
        cache.clear();
      }
    };
  }
}
