package net.io_0.caja.redis;

import io.lettuce.core.codec.RedisCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.nio.ByteBuffer;

@RequiredArgsConstructor
@Slf4j
public class JDKObjectCodec<K, V> implements RedisCodec<K, V> {
  private final String cacheName;

  @Override @SuppressWarnings("unchecked")
  public K decodeKey(ByteBuffer bytes) {
    return (K) ((CacheNameAndKey) decode(bytes)).getKey();
  }

  @Override @SuppressWarnings("unchecked")
  public V decodeValue(ByteBuffer bytes) {
    return (V) decode(bytes);
  }

  @Override
  public ByteBuffer encodeKey(Object key) {
    return encode(new CacheNameAndKey(cacheName, key));
  }

  @Override
  public ByteBuffer encodeValue(Object value) {
    return encode(value);
  }

  private Object decode(ByteBuffer bytes) {
    try {
      byte[] array = new byte[bytes.remaining()];
      bytes.get(array);
      ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(array));
      return is.readObject();
    } catch (Exception e) {
      log.error("Failed to decode", e);
      return null;
    }
  }

  private ByteBuffer encode(Object value) {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ObjectOutputStream os = new ObjectOutputStream(bytes);
      os.writeObject(value);
      return ByteBuffer.wrap(bytes.toByteArray());
    } catch (IOException e) {
      log.error("Failed to encode", e);
      return null;
    }
  }
}
