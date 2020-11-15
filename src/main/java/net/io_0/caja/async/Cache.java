package net.io_0.caja.async;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.util.Objects.nonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Defines all operational methods to create, access, update and delete mappings of key to value.
 * <p>
 * In order to function, cache keys must respect the {@link Object#hashCode() hash code} and
 * {@link Object#equals(Object) equals} contracts. This contract is what will be used to lookup values based on key.
 * <p>
 * A {@code Cache} is not a map, mostly because it has the following two concepts linked to mappings:
 * <ul>
 *   <li>Eviction: A {@code Cache} has a capacity constraint and in order to honor it, a {@code Cache} can
 *   evict (remove) a mapping at any point in time. Note that eviction may occur before maximum capacity is
 *   reached.</li>
 *   <li>Expiry: Data in a {@code Cache} can be configured to expire after some time. There is no way for a
 *   {@code Cache} user to differentiate from the API between a mapping being absent or expired.</li>
 * </ul>
 *
 * @param <K> the key type for the cache
 * @param <V> the value type for the cache
 */
public interface Cache<K, V> {
  /**
   * Retrieves the value currently mapped to the provided key.
   *
   * @param key the key, may not be {@code null}
   * @return the value mapped to the key, {@code null} if none
   *
   * @throws NullPointerException if the provided key is {@code null}
   */
  CompletableFuture<V> get(K key);

  /**
   * Associates the given value to the given key in this {@code Cache}.
   *
   * @param key the key, may not be {@code null}
   * @param value the value, may not be {@code null}
   *
   * @throws NullPointerException if either key or value is {@code null}
   */
  CompletableFuture<Void> put(K key, V value);

  /**
   * Checks whether a mapping for the given key is present, without retrieving the associated value.
   *
   * @param key the key, may not be {@code null}
   * @return {@code true} if a mapping is present, {@code false} otherwise
   *
   * @throws NullPointerException if the provided key is {@code null}
   */
  CompletableFuture<Boolean> containsKey(K key);

  /**
   * Retrieves all currently active keys
   *
   * @return found keys
   */
  CompletableFuture<List<K>> keys();

  /**
   * Removes the value, if any, associated with the provided key.
   *
   * @param key the key to remove the value for, may not be {@code null}
   *
   * @throws NullPointerException if the provided key is {@code null}
   */
  CompletableFuture<Void> remove(K key);

  /**
   * Removes all mappings currently present in the Cache.
   */
  CompletableFuture<Void> clear();

  /**
   * Retrieves the value currently mapped to the provided key. If no key is mapped, the cache will be populated.
   *
   * @param key the key, may not be {@code null}
   * @param valueSupplier value supplier if none is associated with the provided key
   * @return the value mapped to the key, {@code null} if none
   *
   * @throws NullPointerException if the provided key is {@code null}
   */
  default CompletableFuture<V> getThrough(K key, Supplier<V> valueSupplier) {
    return getThroughFuture(key, () -> completedFuture(valueSupplier.get()));
  }

  /**
   * Retrieves the value currently mapped to the provided key. If no key is mapped, the cache will be populated.
   *
   * @param key the key, may not be {@code null}
   * @param valueSupplier value supplier if none is associated with the provided key
   * @return the value mapped to the key, {@code null} if none
   *
   * @throws NullPointerException if the provided key is {@code null}
   */
  default CompletableFuture<V> getThroughFuture(K key, Supplier<CompletableFuture<V>> valueSupplier) {
    return get(key).thenCompose(value ->
      nonNull(value) ? completedFuture(value) : valueSupplier.get()
        .thenCompose(newValue -> put(key, newValue).thenApply(nothing -> newValue))
    );
  }
}
