package net.io_0.caja.ehcache;

import lombok.NoArgsConstructor;
import net.io_0.caja.async.Cache;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class EhcacheAsyncWrapper {
  public static <K, V> Cache<K, V> wrap(org.ehcache.Cache<K, V> cache) {
    return new Cache<>() {
      @Override
      public CompletableFuture<V> get(K key) {
        return supplyAsync(() -> cache.get(key));
      }

      @Override
      public CompletableFuture<Void> put(K key, V value) {
        return runAsync(() -> cache.put(key, value));
      }

      @Override
      public CompletableFuture<Boolean> containsKey(K key) {
        return supplyAsync(() -> cache.containsKey(key));
      }

      @Override
      public CompletableFuture<List<K>> keys() {
        return supplyAsync(() -> stream(cache.spliterator(), false).map(org.ehcache.Cache.Entry::getKey).collect(toList()));
      }

      @Override
      public CompletableFuture<Void> remove(K key) {
        return runAsync(() -> cache.remove(key));
      }

      @Override
      public CompletableFuture<Void> clear() {
        return runAsync(cache::clear);
      }
    };
  }
}
