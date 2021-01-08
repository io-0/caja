package net.io_0.caja.sync;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class CacheDecorator<K, V> implements Cache<K, V> {
  protected final Cache<K, V> cache;
}
