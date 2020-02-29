package net.io_0.caja.redis;

import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JsonObjectCodecTest {
  public static final String CACHE_NAME = "name";

  @Test
  void encodeDecodeKeys() {
    encodeDecodeTest("test", String.class);
    encodeDecodeTest(UUID.randomUUID(), UUID.class);
    encodeDecodeTest(12, Integer.class);
    encodeDecodeTest(24L, Long.class);
  }

  <K> void encodeDecodeTest(K key, Class<K> type) {
    JsonObjectCodec<K, Object> codec = new JsonObjectCodec<>(CACHE_NAME, type, Object.class);

    ByteBuffer encoded = codec.encodeKey(key);
    String probe = StandardCharsets.UTF_8.decode(encoded).toString();
    assertTrue(probe.startsWith(CACHE_NAME), () -> String.format("'%s' doesn't start with '%s'", probe, CACHE_NAME));
    encoded.rewind();
    assertEquals(key, codec.decodeKey(encoded));
  }
}