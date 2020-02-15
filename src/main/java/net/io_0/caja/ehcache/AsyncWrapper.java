package net.io_0.caja.ehcache;

import net.io_0.caja.async.Cache;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class AsyncWrapper {
  public static <K, V> Cache<K, V> wrap(org.ehcache.Cache<K, V> cache) {
    return new Cache<>() {
      @Override
      public CompletableFuture<V> get(K key) {
        return completedFuture(cache.get(key));
      }

      @Override
      public CompletableFuture<Void> put(K key, V value) {
        cache.put(key, value);
        return completedFuture(null);
      }

      @Override
      public CompletableFuture<Boolean> containsKey(K key) {
        return completedFuture(cache.containsKey(key));
      }

      @Override
      public CompletableFuture<Void> remove(K key) {
        cache.remove(key);
        return completedFuture(null);
      }
    };
  }
}
