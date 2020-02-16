package net.io_0.caja.redis;

import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.mapping.Mapper;
import java.io.*;
import java.nio.ByteBuffer;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Slf4j
public class JsonObjectCodec<K, V> implements RedisCodec<K, V> {
  private final String cacheName;
  private final Class<K> keyType;
  private final Class<V> valueType;

  @Override @SuppressWarnings("unchecked")
  public K decodeKey(ByteBuffer bytes) {
    CacheNameAndKey<K> cNAK = (CacheNameAndKey<K>) decode(bytes, CacheNameAndKey.class);
    return (nonNull(cNAK) ? cNAK.key : null);
  }

  @Override @SuppressWarnings("unchecked")
  public V decodeValue(ByteBuffer bytes) {
    return (V) decode(bytes, valueType);
  }

  @Override @SuppressWarnings("unchecked")
  public ByteBuffer encodeKey(Object key) {
    return encode(new CacheNameAndKey<>(cacheName, (K) key));
  }

  @Override
  public ByteBuffer encodeValue(Object value) {
    return encode(value);
  }

  @RequiredArgsConstructor
  @Getter @Setter
  static class CacheNameAndKey<K> implements Serializable {
    private final String cache;
    private final K key;
  }

  private Object decode(ByteBuffer bytes, Class<?> type) {
    return Mapper.fromJson(StringCodec.UTF8.decodeValue(bytes), type);
  }

  private ByteBuffer encode(Object value) {
    return StringCodec.UTF8.encodeValue(Mapper.toJson(value));
  }
}
