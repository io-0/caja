package net.io_0.caja;

import net.io_0.caja.async.Cache;
import net.io_0.caja.configuration.CacheManagerConfig;
import net.io_0.caja.configuration.RemoteCacheConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static net.io_0.caja.AsyncUtils.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Narrative:
 *   As an Cache API consumer
 *
 *   I want a temporary key value store for Java objects
 *   so that I can cache data of my linking
 */
public class RemoteAsyncCacheTest {
  /**
   * Scenario: It should be possible to cache data for a certain time
   */
  @Test
  public void cacheAndRetrieveData() {
    // Given a cache, data and keys
    Cache<String, Integer> one1 = cacheManager1.getAsAsync(CACHE_A, String.class, Integer.class);
    Cache<String, Integer> one2 = cacheManager2.getAsAsync(CACHE_A, String.class, Integer.class);
    Cache<Integer, String> two2 = cacheManager2.getAsAsync(CACHE_B, Integer.class, String.class, new RemoteCacheConfig().setTtlInSeconds(2));

    // When the data is cached
    one1.put(oneKey1, oneValue1);
    one1.put(oneKey2, oneValue2);
    one2.put(oneKey1, oneValue1);
    one2.put(oneKey2, oneValue2);
    two2.put(twoKey1, twoValue1);
    two2.put(twoKey2, twoValue2);

    // Then the data should be retrievable
    assertTrue(await(one1.containsKey(oneKey1)));
    assertTrue(await(one1.containsKey(oneKey2)));
    assertTrue(await(one2.containsKey(oneKey1)));
    assertTrue(await(one2.containsKey(oneKey2)));
    assertTrue(await(two2.containsKey(twoKey1)));
    assertTrue(await(two2.containsKey(twoKey2)));
    assertEquals(oneValue1, await(one1.get(oneKey1)));
    assertEquals(oneValue2, await(one1.get(oneKey2)));
    assertEquals(oneValue1, await(one2.get(oneKey1)));
    assertEquals(oneValue2, await(one2.get(oneKey2)));
    assertEquals(twoValue1, await(two2.get(twoKey1)));
    assertEquals(twoValue2, await(two2.get(twoKey2)));

    // And uncached data should not
    assertFalse(await(one1.containsKey(oneKey3)));
    assertFalse(await(one1.containsKey(oneKey4)));
    assertFalse(await(one2.containsKey(oneKey3)));
    assertFalse(await(one2.containsKey(oneKey4)));
    assertFalse(await(two2.containsKey(twoKey3)));
    assertFalse(await(two2.containsKey(twoKey4)));
    assertNull(await(one1.get(oneKey3)));
    assertNull(await(one1.get(oneKey4)));
    assertNull(await(one2.get(oneKey3)));
    assertNull(await(one2.get(oneKey4)));
    assertNull(await(two2.get(twoKey3)));
    assertNull(await(two2.get(twoKey4)));

    // And neither should data after ttl (timeToLive)
    assertTrue(await(one1.containsKey(oneKey2)));
    assertTrue(await(two2.containsKey(twoKey2)));
    org.awaitility.Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> !await(one1.containsKey(oneKey2)) && !await(two2.containsKey(twoKey2)));
    assertFalse(await(one1.containsKey(oneKey2)));
    assertFalse(await(two2.containsKey(twoKey2)));
  }

  /**
   * Scenario: It should be convenient to work with caches
   */
  @Test
  public void useCacheConveniently() {
    // Given a cache, data and keys
    Cache<String, Integer> one1 = cacheManager1.getAsAsync(CACHE_A, String.class, Integer.class);
    Cache<String, Integer> one2 = cacheManager2.getAsAsync(CACHE_A, String.class, Integer.class);

    // When the data is cached
    one1.put(oneKey1, oneValue1);
    one1.put(oneKey2, oneValue2);
    one2.put(oneKey1, oneValue1);
    one2.put(oneKey2, oneValue2);

    // Then the data should be present
    assertTrue(await(one1.containsKey(oneKey1)));
    assertTrue(await(one1.containsKey(oneKey2)));
    assertTrue(await(one2.containsKey(oneKey1)));
    assertTrue(await(one2.containsKey(oneKey2)));

    // And removed data shouldn't
    one1.remove(oneKey1);
    one2.remove(oneKey1);
    assertFalse(await(one1.containsKey(oneKey1)));
    assertFalse(await(one2.containsKey(oneKey1)));

    // And it should be convenient to retrieve values through the cache
    assertEquals(oneValue1, await(one1.getThrough(oneKey1, () -> oneValue1)));
    assertEquals(oneValue1, await(one2.getThrough(oneKey1, () -> oneValue1)));
    assertEquals(oneValue2, await(one1.getThrough(oneKey2, Assertions::fail)));
    assertEquals(oneValue2, await(one2.getThrough(oneKey2, Assertions::fail)));
    assertThrows(RuntimeException.class, () -> await(one1.getThroughFuture(oneKey3, () -> failedFuture(new IllegalStateException()))));
    assertEquals(oneValue1, await(one1.getThroughFuture(oneKey3, () -> completedFuture(oneValue1))));
    assertEquals(oneValue1, await(one2.getThroughFuture(oneKey3, () -> completedFuture(oneValue1))));
    assertEquals(oneValue2, await(one1.getThroughFuture(oneKey2, Assertions::fail)));
    assertEquals(oneValue2, await(one2.getThroughFuture(oneKey2, Assertions::fail)));

    // And it shouldn't matter which cache reference I use
    assertEquals(await(one1.get(oneKey1)), await(cacheManager1.getAsAsync(CACHE_A, String.class, Integer.class).get(oneKey1)));
    assertEquals(await(one1.get(oneKey2)), await(cacheManager1.getAsAsync(CACHE_A, String.class, Integer.class).get(oneKey2)));
    assertEquals(await(one2.get(oneKey1)), await(cacheManager2.getAsAsync(CACHE_A, String.class, Integer.class).get(oneKey1)));
    assertEquals(await(one2.get(oneKey2)), await(cacheManager2.getAsAsync(CACHE_A, String.class, Integer.class).get(oneKey2)));
  }

  private static final String CACHE_A = "cache A";
  private static final String CACHE_B = "cache B";
  private CacheManager cacheManager1;
  private CacheManager cacheManager2;
  private String oneKey1 = "ok1";
  private String oneKey2 = "ok2";
  private String oneKey3 = "ok3";
  private String oneKey4 = "ok4";
  private Integer twoKey1 = 4001;
  private Integer twoKey2 = 4002;
  private Integer twoKey3 = 4003;
  private Integer twoKey4 = 4004;
  private Integer oneValue1 = 1;
  private Integer oneValue2 = 2;
  private String twoValue1 = "a";
  private String twoValue2 = "b";

  @BeforeEach
  public void init() {
    cacheManager1 = new CacheManager(new RemoteCacheConfig().setTtlInSeconds(2).setHost("redis://localhost:6379/0"));
    cacheManager2 = new CacheManager(new CacheManagerConfig().setCacheConfigurations(
      Map.of(CACHE_A, new RemoteCacheConfig().setTtlInSeconds(1).setHost("redis://localhost:6379/0"))
    ));
    // give remote cache time to reset
    org.awaitility.Awaitility.with().pollDelay(Duration.ofSeconds(2)).await().until(() -> true);
  }

  @AfterEach
  public void finalise() {
    cacheManager1.close();
    cacheManager2.close();
  }
}