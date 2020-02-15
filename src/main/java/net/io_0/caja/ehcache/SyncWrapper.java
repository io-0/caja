package net.io_0.caja.ehcache;

import net.io_0.caja.sync.Cache;

public class SyncWrapper {
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
      public void remove(K key) {
        cache.remove(key);
      }
    };
  }
}
