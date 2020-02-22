package net.io_0.caja.redis;

import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.mapping.Mapper;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.nio.ByteBuffer;

@RequiredArgsConstructor
@Slf4j
public class JsonObjectCodec<K, V> implements RedisCodec<K, V> {
  private final String cacheName;
  private final Class<K> keyType;
  private final Class<V> valueType;

  private final static String SEPARATOR = "/";

  @Override @SuppressWarnings("unchecked")
  public K decodeKey(ByteBuffer bytes) {
    if (keyType.equals(String.class)) {
      return (K) StringUtils.removeStart(cacheName + SEPARATOR, StringCodec.UTF8.decodeValue(bytes));
    }
    return ((NameSpaceAndKey<K>) decode(bytes, NameSpaceAndKey.class)).key;
  }

  @Override @SuppressWarnings("unchecked")
  public V decodeValue(ByteBuffer bytes) {
    return (V) decode(bytes, valueType);
  }

  @Override @SuppressWarnings("unchecked")
  public ByteBuffer encodeKey(Object key) {
    if (keyType.equals(String.class)) {
      return StringCodec.UTF8.encodeValue(cacheName + SEPARATOR + key);
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

  private Object decode(ByteBuffer bytes, Class<?> type) {
    return Mapper.fromJson(StringCodec.UTF8.decodeValue(bytes), type);
  }

  private ByteBuffer encode(Object value) {
    return StringCodec.UTF8.encodeValue(Mapper.toJson(value));
  }
}
