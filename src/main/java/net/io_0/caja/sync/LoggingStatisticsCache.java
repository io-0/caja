package net.io_0.caja.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Slf4j
public class LoggingStatisticsCache<K, V> implements Cache<K, V> {
  private final String name;
  private final Cache<K, V> cache;

  @Override
  public V get(K key) {
    V value = cache.get(key);
    log.debug("{}: {} value for '{}'", name, nonNull(value) ? "got" : "missed", key);
    return value;
  }

  @Override
  public void put(K key, V value) {
    log.debug("{}: put value for '{}'", name, key);
    cache.put(key, value);
  }

  @Override
  public boolean containsKey(K key) {
    boolean isKeyPresent = cache.containsKey(key);
    log.debug("{}: {} key '{}'", name, isKeyPresent ? "contained" : "missed", key);
    return isKeyPresent;
  }

  @Override
  public List<K> keys() {
    List<K> keys = cache.keys();
    log.debug("{}: fetched {} active keys", name, keys.size());
    return keys;
  }

  @Override
  public void remove(K key) {
    log.debug("{}: removed value for '{}'", name, key);
    cache.remove(key);
  }

  @Override
  public void clear() {
    log.debug("{}: cleared", name);
    cache.clear();
  }
}
