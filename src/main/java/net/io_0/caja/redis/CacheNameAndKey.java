package net.io_0.caja.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.io.Serializable;

@RequiredArgsConstructor
public class CacheNameAndKey implements Serializable {
  private final String cacheName;
  @Getter
  private final Object key;
}
