package net.io_0.caja.redis;

import io.lettuce.core.codec.RedisCodec;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.mapping.Mapper;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Predicate;

import static io.lettuce.core.codec.StringCodec.UTF8;
import static org.apache.commons.lang3.StringUtils.removeStart;

@Slf4j
public class JsonObjectCodec<K, C extends KeyOrWildcard<K>, V> implements RedisCodec<C, V> {
  private final String cacheName;
  private final Class<K> keyType;
  private final Class<V> valueType;
  private final Class<?>[] subTypes;

  private static final String SEPARATOR = "/";
  private static final Predicate<Class<?>> isSimpleKeyType = isAnyOf(String.class, UUID.class, Integer.class, Long.class);

  public JsonObjectCodec(String cacheName, Class<K> keyType, Class<V> valueType, Class<?>... subTypes) {
    this.cacheName = cacheName;
    this.keyType = keyType;
    this.valueType = valueType;
    this.subTypes = subTypes;
  }

  @Override @SuppressWarnings("unchecked")
  public C decodeKey(ByteBuffer bytes) {
    if (isSimpleKeyType.test(keyType)) {
      return (C) KeyOrWildcard.key((K) decodeSimpleKey(removeStart(UTF8.decodeValue(bytes), cacheName + SEPARATOR), keyType));
    }
    return (C) KeyOrWildcard.key(Mapper.fromJson(UTF8.decodeValue(bytes), NameSpaceAndKey.class, keyType).key);
  }

  @Override @SuppressWarnings("unchecked")
  public V decodeValue(ByteBuffer bytes) {
    return (V) decode(bytes, valueType, subTypes);
  }

  @Override
  public ByteBuffer encodeKey(KeyOrWildcard keyOrWildcard) {
    if (keyOrWildcard.isWildcard()) {
      if (isSimpleKeyType.test(keyType)) {
        return UTF8.encodeValue(cacheName + SEPARATOR + "*");
      }
      return UTF8.encodeValue("{\"ns\":\""+cacheName+"\",*");
    }

    if (isSimpleKeyType.test(keyType)) {
      return UTF8.encodeValue(cacheName + SEPARATOR + keyOrWildcard.getKey());
    }
    return encode(new NameSpaceAndKey<>(cacheName, keyOrWildcard.getKey()));
  }

  @Override
  public ByteBuffer encodeValue(V value) {
    return encode(value);
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter
  private static class NameSpaceAndKey<K> {
    private String ns;
    private K key;
  }

  private static Object decode(ByteBuffer bytes, Class<?> type, Class<?>... subTypes) {
    return Mapper.fromJson(UTF8.decodeValue(bytes), type, subTypes);
  }

  private static ByteBuffer encode(Object value) {
    return UTF8.encodeValue(Mapper.toJson(value));
  }

  private static Object decodeSimpleKey(String key, Class<?> type) {
    if (type.equals(UUID.class)) {
      return UUID.fromString(key);
    } else if (type.equals(Integer.class)) {
      return Integer.valueOf(key);
    } else if (type.equals(Long.class)) {
      return Long.valueOf(key);
    }
    return key;
  }

  private static Predicate<Class<?>> isAnyOf(Class<?>... types) {
    return type -> Arrays.asList(types).contains(type);
  }
}
