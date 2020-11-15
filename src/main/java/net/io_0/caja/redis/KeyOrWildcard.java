package net.io_0.caja.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class KeyOrWildcard<K> {
  private final K key;

  public static <K> KeyOrWildcard<K> key(K k) {
    requireNonNull(k);
    return new KeyOrWildcard<>(k);
  }

  public static <K> KeyOrWildcard<K> wildcard() {
    return new KeyOrWildcard<>(null);
  }

  public boolean isWildcard() {
    return isNull(key);
  }
}
