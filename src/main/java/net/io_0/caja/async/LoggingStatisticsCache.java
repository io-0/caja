package net.io_0.caja.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Slf4j
public class LoggingStatisticsCache<K, V> implements Cache<K, V> {
  private final String name;
  private final Cache<K, V> cache;

  @Override
  public CompletableFuture<V> get(K key) {
    return cache.get(key)
      .whenComplete((value, error) -> {
        if (isNull(error)) log.debug("{}: {} value for '{}'", name, nonNull(value) ? "got" : "missed", key);
      });
  }

  @Override
  public CompletableFuture<Void> put(K key, V value) {
    log.debug("{}: put value for '{}'", name, key);
    return cache.put(key, value);
  }

  @Override
  public CompletableFuture<Boolean> containsKey(K key) {
    return cache.containsKey(key)
      .whenComplete((value, error) -> {
        if (isNull(error) && nonNull(value)) log.debug("{}: {} key '{}'", name, value ? "contained" : "missed", key);
      });
  }

  @Override
  public CompletableFuture<Void> remove(K key) {
    log.debug("{}: removed value for '{}'", name, key);
    return cache.remove(key);
  }
}
