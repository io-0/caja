package net.io_0.caja;

import net.io_0.caja.configuration.CacheManagerConfig;
import net.io_0.caja.configuration.LocalCacheConfig;
import net.io_0.caja.configuration.RemoteCacheConfig;
import net.io_0.caja.async.Cache;
import org.junit.jupiter.api.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.Instant.now;
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
public class CacheTestAsync {
  /**
   * Scenario: It should be possible to cache data for a certain time
   */
  @Test
  public void cacheAndRetrieveData() {
    // Given a cache, data and keys
    List<Cache<String, Integer>> aCaches = setupCaches(String.class, Integer.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5);
    List<Cache<Integer, String>> bCaches = List.of(
      cacheManager3.getAsAsync(CACHE_B, Integer.class, String.class, new LocalCacheConfig().setTtlInSeconds(2).setHeap(5)),
      cacheManager5.getAsAsync(CACHE_B, Integer.class, String.class, new LocalCacheConfig().setTtlInSeconds(2))
    );

    // When the data is cached
    fillCaches(aCaches, Map.of(oneKey1, oneValue1, oneKey2, oneValue2));
    fillCaches(bCaches, Map.of(twoKey1, twoValue1, twoKey2, twoValue2));

    // Then the data should be retrievable
    assertKeysPresent(aCaches, List.of(oneKey1, oneKey2));
    assertKeysPresent(bCaches, List.of(twoKey1, twoKey2));
    assertValuesPresent(aCaches, Map.of(oneKey1, oneValue1, oneKey2, oneValue2));
    assertValuesPresent(bCaches, Map.of(twoKey1, twoValue1, twoKey2, twoValue2));

    // And uncached data should not
    assertKeysAbsent(aCaches, List.of(oneKey3, oneKey4));
    assertKeysAbsent(bCaches, List.of(twoKey3, twoKey4));
    assertValuesAbsent(aCaches, List.of(oneKey3, oneKey4));
    assertValuesAbsent(bCaches, List.of(twoKey3, twoKey4));

    // And neither should data after ttl (timeToLive)
    fillCaches(List.of(aCaches.get(0), aCaches.get(2)), Map.of(oneKey3, oneValue1));
    assertKeysAbsentAfterTtl(Map.of(aCaches.get(0), oneKey3, aCaches.get(2), oneKey3), 1);
    emptyCaches(List.of(aCaches.get(0), aCaches.get(2)), List.of(oneKey3));

    fillCaches(List.of(aCaches.get(1), aCaches.get(3)), Map.of(oneKey4, oneValue1));
    fillCaches(bCaches, Map.of(twoKey3, twoValue1, twoKey4, twoValue2));
    assertKeysAbsentAfterTtl(Map.of(aCaches.get(1), oneKey4, bCaches.get(0), twoKey4, aCaches.get(3), oneKey4, bCaches.get(1), twoKey4), 2);
    emptyCaches(List.of(aCaches.get(1), aCaches.get(3)), List.of(oneKey4));
    emptyCaches(bCaches, List.of(twoKey3, twoKey4));
  }

  /**
   * Scenario: It should be convenient to work with caches
   */
  @Test
  public void useCacheConveniently() {
    // Given a cache, data and keys
    List<Cache<String, Integer>> aCaches = setupCaches(String.class, Integer.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5);

    // When the data is cached
    fillCaches(aCaches, Map.of(oneKey1, oneValue1, oneKey2, oneValue2));

    // Then the data should be present
    assertKeysPresent(aCaches, List.of(oneKey1, oneKey2));

    // And removed data shouldn't
    emptyCaches(aCaches, List.of(oneKey1));
    assertKeysAbsent(aCaches, List.of(oneKey1));

    // And it should be convenient to retrieve values through the cache
    assertGetTroughFillsCache(aCaches, Map.of(oneKey1, oneValue1));
    assertGetTroughUsesCache(aCaches, Map.of(oneKey2, oneValue2));
    assertThrows(RuntimeException.class, () -> await(aCaches.get(0).getThroughFuture(oneKey3, () -> failedFuture(new IllegalStateException()))));
    assertThrows(RuntimeException.class, () -> await(aCaches.get(3).getThroughFuture(oneKey3, () -> failedFuture(new IllegalStateException()))));
    assertGetTroughFutureFillsCache(aCaches, Map.of(oneKey3, oneValue1));
    assertGetTroughFutureUsesCache(aCaches, Map.of(oneKey2, oneValue2));

    // And it shouldn't matter which cache reference I use
    assertCacheRefDoesntMatter(aCaches, setupCaches(String.class, Integer.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5), List.of(oneKey1, oneKey1));
  }

  private static final String CACHE_A = "cache A " + now().getNano();
  private static final String CACHE_B = "cache B " + now().getNano();
  private CacheManager cacheManager1;
  private CacheManager cacheManager2;
  private CacheManager cacheManager3;
  private CacheManager cacheManager4;
  private CacheManager cacheManager5;
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
    cacheManager1 = new CacheManager();
    cacheManager2 = new CacheManager(new LocalCacheConfig().setHeap(10).setTtlInSeconds(2));
    cacheManager3 = new CacheManager(new CacheManagerConfig().setCacheConfigurations(Map.of(CACHE_A, new LocalCacheConfig().setTtlInSeconds(1).setHeap(5))));
    cacheManager4 = new CacheManager(new RemoteCacheConfig().setTtlInSeconds(2).setHost("redis://localhost:6379/0"));
    cacheManager5 = new CacheManager(new CacheManagerConfig().setCacheConfigurations(
      Map.of(CACHE_A, new RemoteCacheConfig().setTtlInSeconds(1).setHost("redis://localhost:6379/0"))
    ));
  }

  @AfterEach
  public void finalise() {
    cacheManager1.close();
    cacheManager2.close();
    cacheManager3.close();
    cacheManager4.close();
    cacheManager5.close();
  }

  private <K, V> List<Cache<K, V>> setupCaches(Class<K> keyType, Class<V> valueType, CacheManager... managers) {
    return Arrays.stream(managers)
      .map(m -> m.getAsAsync(CACHE_A, keyType, valueType))
      .collect(Collectors.toList());
  }

  private <K, V> void fillCaches(List<Cache<K, V>> caches, Map<K, V> data) {
    caches.forEach(c -> data.forEach((k, v) -> await(c.put(k, v))));
  }

  private <K, V> void emptyCaches(List<Cache<K, V>> caches, List<K> keys) {
    caches.forEach(c -> keys.forEach(k -> await(c.remove(k))));
  }

  private <K, V> void assertKeysPresent(List<Cache<K, V>> caches, List<K> keys) {
    caches.forEach(c -> keys.forEach(k -> assertTrue(await(c.containsKey(k)))));
  }

  private <K, V> void assertKeysAbsent(List<Cache<K, V>> caches, List<K> keys) {
    caches.forEach(c -> keys.forEach(k -> assertFalse(await(c.containsKey(k)))));
  }

  private <K, V> void assertValuesPresent(List<Cache<K, V>> caches, Map<K, V> data) {
    caches.forEach(c -> data.forEach((k, v) -> assertEquals(v, await(c.get(k)))));
  }

  private <K, V> void assertValuesAbsent(List<Cache<K, V>> caches, List<K> keys) {
    caches.forEach(c -> keys.forEach(k -> assertNull(await(c.get(k)))));
  }

  private void assertKeysAbsentAfterTtl(Map<Cache, ?> cachesAndKeys, int ttlInSeconds) {
    cachesAndKeys.forEach((cache, key) -> assertKeysPresent(List.of(cache), List.of(key)));
    org.awaitility.Awaitility.await().atMost(ttlInSeconds, TimeUnit.SECONDS).until(() -> allKeysAbsent(cachesAndKeys));
    cachesAndKeys.forEach((cache, key) -> assertKeysAbsent(List.of(cache), List.of(key)));
  }

  private <K, V> void assertGetTroughFillsCache(List<Cache<K, V>> caches, Map<K, V> data) {
    caches.forEach(c -> data.forEach((k, v) -> assertEquals(v, await(c.getThrough(k, () -> v)))));
  }

  private <K, V> void assertGetTroughUsesCache(List<Cache<K, V>> caches, Map<K, V> data) {
    caches.forEach(c -> data.forEach((k, v) -> assertEquals(v, await(c.getThrough(k, Assertions::fail)))));
  }

  private <K, V> void assertGetTroughFutureFillsCache(List<Cache<K, V>> caches, Map<K, V> data) {
    caches.forEach(c -> data.forEach((k, v) -> assertEquals(v, await(c.getThroughFuture(k, () -> completedFuture(v))))));
  }

  private <K, V> void assertGetTroughFutureUsesCache(List<Cache<K, V>> caches, Map<K, V> data) {
    caches.forEach(c -> data.forEach((k, v) -> assertEquals(v, await(c.getThroughFuture(k, Assertions::fail)))));
  }

  private <K, V> void assertCacheRefDoesntMatter(List<Cache<K, V>> aCaches, List<Cache<K, V>> bCaches, List<K> keys) {
    IntStream.range(0, aCaches.size()).forEach(i ->
      keys.forEach(k -> assertEquals(await(aCaches.get(i).get(k)), await(bCaches.get(i).get(k))))
    );
  }

  private boolean allKeysAbsent(Map<Cache, ?> cachesAndKeys) {
    return cachesAndKeys.entrySet().stream()
      .map(e -> allKeysAbsent(List.of(e.getKey()), List.of(e.getValue())))
      .reduce(true, Boolean::logicalAnd);
  }

  private <K, V> boolean allKeysAbsent(List<Cache<K, V>> caches, List<K> keys) {
    // !cache1.containsKey(key1) && !cache2.containsKey(key1) = !(cache1.containsKey(key1) || cache2.containsKey(key1))
    return !caches.stream()
      .flatMap(c -> keys.stream()
      .map(c::containsKey))
      .map(AsyncUtils::await)
      .reduce(false, Boolean::logicalOr);
  }
}