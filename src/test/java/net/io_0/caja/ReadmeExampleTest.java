package net.io_0.caja;

import net.io_0.caja.sync.Cache;
import net.io_0.caja.configuration.CacheManagerConfig;
import net.io_0.caja.configuration.LocalCacheConfig;
import net.io_0.caja.configuration.RemoteCacheConfig;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static net.io_0.caja.AsyncUtils.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ReadmeExampleTest {
  @Test
  void exampleTest() {
    // Given
    // All caches retrieved from this will be local caches with heap 100 and ttl 1 second.
    CacheManager cacheManager1 = new CacheManager();

    // All caches retrieved from this will be local caches with heap 10 and ttl 2 second.
    CacheManager cacheManager2 = new CacheManager(new LocalCacheConfig().setHeap(10).setTtlInSeconds(2));

    // All caches retrieved from this will be local caches with heap 100 and ttl 1 second, with one exception:
    // If the cache is called 'my cache', it's heap will be 5 and it's ttl 2 seconds.
    CacheManager cacheManager3 = new CacheManager(new CacheManagerConfig().setCacheConfigurations(Map.of("my cache", new LocalCacheConfig().setHeap(5).setTtlInSeconds(2))));

    // All caches retrieved from this will be remote caches with ttl 2 second.
    CacheManager cacheManager4 = new CacheManager(new RemoteCacheConfig().setTtlInSeconds(2).setHost("redis://localhost:6379/0"));

    // All caches retrieved from this will be remote caches with ttl 1 second, with one exception:
    // If the cache is called 'my cache', it will be remote with ttl 2 seconds.
    CacheManager cacheManager5 = new CacheManager(new CacheManagerConfig()
      .setDefaultCacheConfiguration(new RemoteCacheConfig().setHost("redis://localhost:6379/0"))
      .setCacheConfigurations(Map.of("my cache", new RemoteCacheConfig().setTtlInSeconds(2).setHost("redis://localhost:6379/0")))
    );

    // When
    Cache<Integer, String> shortTermCache = cacheManager1.getAsSync("short term cache", Integer.class, String.class);

    System.out.println(shortTermCache.containsKey(1)); // prints false
    shortTermCache.put(2, "two");
    System.out.println(shortTermCache.get(2)); // prints 'two'
    System.out.println(shortTermCache.getThrough(2, () -> "three")); // prints 'two', after ttl expires it would print 'three' and populate the cache

    net.io_0.caja.async.Cache<Integer, String> shortTermCacheAsync = cacheManager1.getAsAsync("short term cache", Integer.class, String.class);
    System.out.println(await(shortTermCacheAsync.get(2))); // prints 'two', same cache with async interface

    // Then
    assertFalse(shortTermCache.containsKey(1));
    assertEquals("two", shortTermCache.get(2));
    assertEquals("two", shortTermCache.getThrough(2, () -> "three"));
    assertEquals("two", await(shortTermCacheAsync.get(2)));
    org.awaitility.Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> Objects.equals("three", shortTermCache.getThrough(2, () -> "three")));
  }
}
