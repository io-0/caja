package net.io_0.caja;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Slf4j
public class LoggingStatisticsCache<K, V> implements Cache<K, V> {
  private final String name;
  private final Cache<K, V> cache;

  @Override
  public V get(K key) {
    V value = cache.get(key);
    log.trace("{}: {} value for '{}'", name, nonNull(value)? "got" : "missed", key);
    return value;
  }

  @Override
  public void put(K key, V value) {
    log.trace("{}: put value for '{}'", name, key);
    cache.put(key, value);
  }

  @Override
  public boolean containsKey(K key) {
    boolean isKeyPresent = cache.containsKey(key);
    log.trace("{}: {} key '{}'", name, isKeyPresent ? "contained" : "missed", key);
    return isKeyPresent;
  }

  @Override
  public void remove(K key) {
    log.trace("{}: removed value for '{}'", name, key);
    cache.remove(key);
  }
}
