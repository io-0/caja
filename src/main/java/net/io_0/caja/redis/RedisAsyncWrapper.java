package net.io_0.caja.redis;

import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.NoArgsConstructor;
import net.io_0.caja.async.Cache;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.*;

@NoArgsConstructor(access = PRIVATE)
public class RedisAsyncWrapper {
  public static <K, V> Cache<K, V> wrap(RedisAsyncCommands<KeyOrWildcard<K>, V> cache, Integer ttlInSeconds) {
    return new Cache<>() {
      @Override
      public CompletableFuture<V> get(K key) {
        requireNonNull(key);
        return cache.get(KeyOrWildcard.key(key)).toCompletableFuture();
      }

      @Override
      public CompletableFuture<Void> put(K key, V value) {
        requireNonNull(key);
        return cache.setex(KeyOrWildcard.key(key), ttlInSeconds, value).toCompletableFuture().thenApply(ignore -> null);
      }

      @Override @SuppressWarnings("unchecked")
      public CompletableFuture<Boolean> containsKey(K key) {
        requireNonNull(key);
        return cache.exists(KeyOrWildcard.key(key)).toCompletableFuture().thenApply(keyCount -> keyCount > 0);
      }

      @Override
      public CompletableFuture<List<K>> keys() {
        return fetchKeys().thenApply(containers -> containers.stream()
          .map(KeyOrWildcard::getKey)
          .collect(toList())
        );
      }

      private CompletableFuture<List<KeyOrWildcard<K>>> fetchKeys() {
        return cache.keys(KeyOrWildcard.wildcard()).toCompletableFuture();
      }

      @Override @SuppressWarnings("unchecked")
      public CompletableFuture<Void> remove(K key) {
        requireNonNull(key);
        return cache.del(KeyOrWildcard.key(key)).toCompletableFuture().thenApply(ignore -> null);
      }

      @SuppressWarnings("unchecked")
      @Override
      public CompletableFuture<Void> clear() {
        return fetchKeys().thenCompose(keys -> CompletableFuture.allOf(
          keys.stream()
            .map(cache::del)
            .map(CompletionStage::toCompletableFuture)
            .toArray(CompletableFuture[]::new)
        ));
      }
    };
  }
}
