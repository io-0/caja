package net.io_0.caja.async;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.io_0.caja.LoggingUtils.logThrough;
import static net.io_0.caja.configuration.CacheConfig.*;

@Slf4j
public class LoggingStatisticsDecorator<K, V> extends CacheDecorator<K, V> {
  private final String name;
  private final LogLevel logLevel;

  public LoggingStatisticsDecorator(String name, LogLevel logLevel, Cache<K, V> cache) {
    super(cache);
    this.name = name;
    this.logLevel = logLevel;
  }

  @Override
  public CompletableFuture<V> get(K key) {
    return cache.get(key)
      .whenComplete((value, error) -> {
        if (isNull(error)) log("{}: {} value for '{}'", name, nonNull(value) ? "got" : "missed", key);
      });
  }

  @Override
  public CompletableFuture<Void> put(K key, V value) {
    return cache.put(key, value)
      .whenComplete((ignored, error) -> {
        if (isNull(error)) log("{}: put value for '{}'", name, key);
      });
  }

  @Override
  public CompletableFuture<Boolean> containsKey(K key) {
    return cache.containsKey(key)
      .whenComplete((value, error) -> {
        if (isNull(error) && nonNull(value)) log("{}: {} key '{}'", name, value ? "contained" : "missed", key);
      });
  }

  @Override
  public CompletableFuture<List<K>> keys() {
    return cache.keys()
      .whenComplete((keys, error) -> {
        if (isNull(error)) log("{}: fetched {} active keys", name, keys.size());
      });
  }

  @Override
  public CompletableFuture<Void> remove(K key) {
    return cache.remove(key)
      .whenComplete((ignored, error) -> {
        if (isNull(error)) log("{}: removed value for '{}'", name, key);
      });
  }

  @Override
  public CompletableFuture<Void> clear() {
    return cache.clear()
      .whenComplete((ignored, error) -> {
        if (isNull(error)) log("{}: cleared", name);
      });
  }

  private void log(String format, Object... arguments) {
    logThrough(log, this.logLevel, format, arguments);
  }
}
