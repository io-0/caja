package net.io_0.caja.redis;

import io.lettuce.core.codec.RedisCodec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.mapping.Mapper;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Predicate;

import static io.lettuce.core.codec.StringCodec.UTF8;
import static org.apache.commons.lang3.StringUtils.removeStart;

@RequiredArgsConstructor
@Slf4j
public class JsonObjectCodec<K, V> implements RedisCodec<K, V> {
  private final String cacheName;
  private final Class<K> keyType;
  private final Class<V> valueType;

  private final static String SEPARATOR = "/";
  private final static Predicate<Class<?>> isSimpleKeyType = isAnyOf(String.class, UUID.class, Integer.class, Long.class);

  @Override @SuppressWarnings("unchecked")
  public K decodeKey(ByteBuffer bytes) {
    if (isSimpleKeyType.test(keyType)) {
      return (K) decodeSimpleKey(removeStart(UTF8.decodeValue(bytes), cacheName + SEPARATOR), keyType);
    }
    return ((NameSpaceAndKey<K>) decode(bytes, NameSpaceAndKey.class)).key;
  }

  @Override @SuppressWarnings("unchecked")
  public V decodeValue(ByteBuffer bytes) {
    return (V) decode(bytes, valueType);
  }

  @Override @SuppressWarnings("unchecked")
  public ByteBuffer encodeKey(Object key) {
    if (isSimpleKeyType.test(keyType)) {
      return UTF8.encodeValue(cacheName + SEPARATOR + key);
    }
    return encode(new NameSpaceAndKey<>(cacheName, (K) key));
  }

  @Override
  public ByteBuffer encodeValue(Object value) {
    return encode(value);
  }

  @RequiredArgsConstructor
  @Getter @Setter
  private static class NameSpaceAndKey<K> implements Serializable {
    private final String ns;
    private final K key;
  }

  private static Object decode(ByteBuffer bytes, Class<?> type) {
    return Mapper.fromJson(UTF8.decodeValue(bytes), type);
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
