package net.io_0.caja.redis;

import io.lettuce.core.api.async.RedisAsyncCommands;
import net.io_0.caja.async.Cache;
import java.util.concurrent.CompletableFuture;

public class RedisAsyncWrapper {
  public static <K, V> Cache<K, V> wrap(RedisAsyncCommands<K, V> cache, Integer ttlInSeconds) {
    return new Cache<>() {
      @Override
      public CompletableFuture<V> get(K key) {
        return cache.get(key).toCompletableFuture();
      }

      @Override
      public CompletableFuture<Void> put(K key, V value) {
        return cache.setex(key, ttlInSeconds, value).toCompletableFuture().thenApply(ignore -> null);
      }

      @Override @SuppressWarnings("unchecked")
      public CompletableFuture<Boolean> containsKey(K key) {
        return cache.exists(key).toCompletableFuture().thenApply(keyCount -> keyCount > 0);
      }

      @Override @SuppressWarnings("unchecked")
      public CompletableFuture<Void> remove(K key) {
        return cache.del(key).toCompletableFuture().thenApply(ignore -> null);
      }
    };
  }
}
