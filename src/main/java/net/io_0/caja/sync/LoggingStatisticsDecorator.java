package net.io_0.caja.sync;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

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
  public V get(K key) {
    V value = cache.get(key);
    log("{}: {} value for '{}'", name, nonNull(value) ? "got" : "missed", key);
    return value;
  }

  @Override
  public void put(K key, V value) {
    log("{}: put value for '{}'", name, key);
    cache.put(key, value);
  }

  @Override
  public boolean containsKey(K key) {
    boolean isKeyPresent = cache.containsKey(key);
    log("{}: {} key '{}'", name, isKeyPresent ? "contained" : "missed", key);
    return isKeyPresent;
  }

  @Override
  public List<K> keys() {
    List<K> keys = cache.keys();
    log("{}: fetched {} active keys", name, keys.size());
    return keys;
  }

  @Override
  public void remove(K key) {
    log("{}: removed value for '{}'", name, key);
    cache.remove(key);
  }

  @Override
  public void clear() {
    log("{}: cleared", name);
    cache.clear();
  }
  
  private void log(String format, Object... arguments) {
    logThrough(log, this.logLevel, format, arguments);
  }
}
