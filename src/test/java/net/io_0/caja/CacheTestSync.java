package net.io_0.caja;

import net.io_0.caja.configuration.CacheManagerConfig;
import net.io_0.caja.configuration.LocalCacheConfig;
import net.io_0.caja.configuration.RemoteCacheConfig;
import net.io_0.caja.models.ComplexKey;
import net.io_0.caja.models.ComplexValue;
import net.io_0.caja.models.Nested;
import net.io_0.caja.sync.Cache;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.*;
import static java.time.Instant.now;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static net.io_0.caja.Asserts.assertCollectionEquals;
import static net.io_0.caja.AsyncUtils.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Narrative:
 *   As an Cache API consumer
 *
 *   I want a temporary key value store for Java objects
 *   so that I can cache data of my linking
 */
class CacheTestSync {
  /**
   * Scenario: It should be possible to cache data for a certain time
   */
  @Test
  void cacheAndRetrieveData() {
    // Given a cache, data and keys
    List<Cache<String, Integer>> aCaches = setupCaches(CACHE_A, String.class, Integer.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5);
    List<Cache<Integer, String>> bCaches = List.of(
      cacheManager3.getAsSync(CACHE_B, Integer.class, String.class, new LocalCacheConfig().setTtlInSeconds(2).setHeap(5)),
      cacheManager5.getAsSync(CACHE_B, Integer.class, String.class, new LocalCacheConfig().setTtlInSeconds(2))
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
  void useCacheConveniently() {
    // Given a cache, data and keys
    List<Cache<String, Integer>> aCaches = setupCaches(CACHE_A, String.class, Integer.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5);

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
    assertCacheRefDoesntMatter(aCaches, setupCaches(CACHE_A, String.class, Integer.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5), List.of(oneKey1, oneKey1));
  }

  /**
   * Scenario: Caches with different names but same keys shouldn't interact
   */
  @Test
  void cachesShouldNotInteract() {
    // Given a cache, data and keys
    List<Cache<String, Integer>> aCaches = setupCaches(CACHE_A, String.class, Integer.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5);
    List<Cache<String, Integer>> bCaches = setupCaches(CACHE_B, String.class, Integer.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5);

    // When the data is cached
    fillCaches(aCaches, Map.of(oneKey1, oneValue1, oneKey2, oneValue2));

    // Then the data should be retrievable
    assertKeysPresent(aCaches, List.of(oneKey1, oneKey2));
    assertKeysAbsent(bCaches, List.of(oneKey1, oneKey2));
    assertValuesPresent(aCaches, Map.of(oneKey1, oneValue1, oneKey2, oneValue2));
    assertValuesAbsent(bCaches, List.of(oneKey1, oneKey2));
  }

  /**
   * Scenario: It should be possible to fetch all cache keys at a given time
   */
  @Test
  void cachedKeysShouldBeRetrievable() {
    // Given a cache, data and keys
    List<Cache<String, Integer>> aCaches = setupCaches(CACHE_A, String.class, Integer.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5);
    List<Cache<String, Integer>> bCaches = setupCaches(CACHE_B, String.class, Integer.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5);
    List<Cache<ComplexKey, ComplexValue>> cCaches = setupCaches(CACHE_C, ComplexKey.class, ComplexValue.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5);

    fillCaches(aCaches, Map.of(oneKey1, oneValue1, oneKey2, oneValue2));
    fillCaches(bCaches, Map.of(oneKey3, oneValue3, oneKey4, oneValue4));
    fillCaches(cCaches, Map.of(complexKey1, complexValue1, complexKey2, complexValue2));

    assertKeysPresent(aCaches, List.of(oneKey1, oneKey2));
    assertKeysPresent(bCaches, List.of(oneKey3, oneKey4));
    assertKeysPresent(cCaches, List.of(complexKey1, complexKey2));

    // When the keys are fetched
    var firstAKeys = aCaches.stream().map(Cache::keys).collect(Collectors.toList());
    var firstBKeys = bCaches.stream().map(Cache::keys).collect(Collectors.toList());
    var firstCKeys = cCaches.stream().map(Cache::keys).collect(Collectors.toList());
    var secondAKeys = aCaches.stream().map(Cache::keys).collect(Collectors.toList());
    var secondBKeys = bCaches.stream().map(Cache::keys).collect(Collectors.toList());
    var secondCKeys = cCaches.stream().map(Cache::keys).collect(Collectors.toList());
    org.awaitility.Awaitility.await().pollInterval(2*1000 + 200, TimeUnit.MILLISECONDS).until(() -> true);
    var thirdAKeys = aCaches.stream().map(Cache::keys).collect(Collectors.toList());
    var thirdBKeys = bCaches.stream().map(Cache::keys).collect(Collectors.toList());
    var thirdCKeys = cCaches.stream().map(Cache::keys).collect(Collectors.toList());

    // Then the right amount of keys should be present at the right time
    firstAKeys.forEach(ks -> assertCollectionEquals(List.of(oneKey1, oneKey2), ks));
    firstBKeys.forEach(ks -> assertCollectionEquals(List.of(oneKey3, oneKey4), ks));
    firstCKeys.forEach(ks -> assertCollectionEquals(List.of(complexKey1, complexKey2), ks));

    assertCollectionEquals(firstAKeys, secondAKeys);
    assertCollectionEquals(firstBKeys, secondBKeys);
    assertCollectionEquals(firstCKeys, secondCKeys);

    thirdAKeys.forEach(ks -> assertEquals(0, ks.size()));
    thirdBKeys.forEach(ks -> assertEquals(0, ks.size()));
    thirdCKeys.forEach(ks -> assertEquals(0, ks.size()));
  }

  /**
   * Scenario: It should be possible to clear a cache
   */
  @Test
  void cachesShouldBeClearable() {
    // Given a cache, data and keys
    List<Cache<String, Integer>> aCaches = setupCaches(CACHE_A, String.class, Integer.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5);
    List<Cache<String, Integer>> bCaches = setupCaches(CACHE_B, String.class, Integer.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5);
    List<Cache<ComplexKey, ComplexValue>> cCaches = setupCaches(CACHE_C, ComplexKey.class, ComplexValue.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5);

    fillCaches(aCaches, Map.of(oneKey1, oneValue1, oneKey2, oneValue2));
    fillCaches(bCaches, Map.of(oneKey3, oneValue3, oneKey4, oneValue4));
    fillCaches(cCaches, Map.of(complexKey1, complexValue1, complexKey2, complexValue2));

    assertKeysPresent(aCaches, List.of(oneKey1, oneKey2));
    assertKeysPresent(bCaches, List.of(oneKey3, oneKey4));
    assertKeysPresent(cCaches, List.of(complexKey1, complexKey2));
    assertValuesPresent(aCaches, Map.of(oneKey1, oneValue1, oneKey2, oneValue2));
    assertValuesPresent(bCaches, Map.of(oneKey3, oneValue3, oneKey4, oneValue4));
    assertValuesPresent(cCaches, Map.of(complexKey1, complexValue1, complexKey2, complexValue2));

    // When the cache is cleared
    bCaches.forEach(Cache::clear);
    bCaches.forEach(Cache::clear);
    cCaches.forEach(Cache::clear);
    cCaches.forEach(Cache::clear);

    // Then the data shouldn't be present
    assertKeysPresent(aCaches, List.of(oneKey1, oneKey2));
    assertKeysAbsent(bCaches, List.of(oneKey1, oneKey2));
    assertKeysAbsent(cCaches, List.of(complexKey1, complexKey2));
    assertValuesPresent(aCaches, Map.of(oneKey1, oneValue1, oneKey2, oneValue2));
    assertValuesAbsent(bCaches, List.of(oneKey1, oneKey2));
    assertValuesAbsent(cCaches, List.of(complexKey1, complexKey2));
  }

  /**
   * Scenario: Caches should be able to handle non primitive data
   */
  @Test
  void cachesShouldHandleNonPrimitives() {
    // Given a cache, data and keys
    List<Cache<ComplexKey, ComplexValue>> aCaches = setupCaches(CACHE_A, ComplexKey.class, ComplexValue.class, cacheManager1, cacheManager2, cacheManager3, cacheManager4, cacheManager5);

    // When the data is cached
    fillCaches(aCaches, Map.of(complexKey1, complexValue1, complexKey2, complexValue2));

    // Then the data should be retrievable
    assertKeysPresent(aCaches, List.of(complexKey1, complexKey2));
    assertValuesPresent(aCaches, Map.of(complexKey1, complexValue1, complexKey2, complexValue2));
  }

  private static final String CACHE_A = "cache A " + now().getNano();
  private static final String CACHE_B = "cache B " + now().getNano();
  private static final String CACHE_C = "cache C " + now().getNano();
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
  private Integer oneValue3 = 3;
  private Integer oneValue4 = 4;
  private String twoValue1 = "a";
  private String twoValue2 = "b";
  private ComplexKey complexKey1 = new ComplexKey("k1", 1, new net.io_0.caja.models.Nested(true, List.of(1, 2, 3)));
  private ComplexValue complexValue1 = new ComplexValue(1L, BigDecimal.TEN, LocalDateTime.now(), new net.io_0.caja.models.Nested(true, List.of(10, 20, 30)));
  private ComplexKey complexKey2 = new ComplexKey("k2", 2, new net.io_0.caja.models.Nested(true, List.of(2, 4, 6)));
  private ComplexValue complexValue2 = new ComplexValue(2L, BigDecimal.ONE, LocalDateTime.now(), new Nested(true, List.of(20, 40, 60)));

  @BeforeEach
  void init() {
    cacheManager1 = new CacheManager();
    cacheManager2 = new CacheManager(new LocalCacheConfig().setHeap(10).setTtlInSeconds(2));
    cacheManager3 = new CacheManager(new CacheManagerConfig().setCacheConfigurations(Map.of(CACHE_A, new LocalCacheConfig().setTtlInSeconds(1).setHeap(5))));
    cacheManager4 = new CacheManager(new RemoteCacheConfig().setTtlInSeconds(2).setHost("redis://localhost:6379/0"));
    cacheManager5 = new CacheManager(new CacheManagerConfig().setCacheConfigurations(
      Map.of(CACHE_A, new RemoteCacheConfig().setTtlInSeconds(1).setHost("redis://localhost:6379/0"))
    ));
  }

  @AfterEach
  void finalise() {
    cacheManager1.close();
    cacheManager2.close();
    cacheManager3.close();
    cacheManager4.close();
    cacheManager5.close();
  }

  private <K, V> List<Cache<K, V>> setupCaches(String name, Class<K> keyType, Class<V> valueType, CacheManager... managers) {
    return Arrays.stream(managers)
      .map(m -> m.getAsSync(name, keyType, valueType))
      .collect(Collectors.toList());
  }

  private <K, V> void fillCaches(List<Cache<K, V>> caches, Map<K, V> data) {
    caches.forEach(c -> data.forEach(c::put));
  }

  private <K, V> void emptyCaches(List<Cache<K, V>> caches, List<K> keys) {
    caches.forEach(c -> keys.forEach(c::remove));
  }

  private <K, V> void assertKeysPresent(List<Cache<K, V>> caches, List<K> keys) {
    caches.forEach(c -> keys.forEach(k -> assertTrue(c.containsKey(k), format("Cache %d", caches.indexOf(c)+1))));
  }

  private <K, V> void assertKeysAbsent(List<Cache<K, V>> caches, List<K> keys) {
    caches.forEach(c -> keys.forEach(k -> assertFalse(c.containsKey(k), format("Cache %d", caches.indexOf(c)+1))));
  }

  private <K, V> void assertValuesPresent(List<Cache<K, V>> caches, Map<K, V> data) {
    caches.forEach(c -> data.forEach((k, v) -> assertEquals(v, c.get(k), format("Cache %d", caches.indexOf(c)+1))));
  }

  private <K, V> void assertValuesAbsent(List<Cache<K, V>> caches, List<K> keys) {
    caches.forEach(c -> keys.forEach(k -> assertNull(c.get(k), format("Cache %d", caches.indexOf(c)+1))));
  }

  private void assertKeysAbsentAfterTtl(Map<Cache, ?> cachesAndKeys, int ttlInSeconds) {
    cachesAndKeys.forEach((cache, key) -> assertKeysPresent(List.of(cache), List.of(key)));
    org.awaitility.Awaitility.await().atMost(ttlInSeconds*1000 + 200, TimeUnit.MILLISECONDS).until(() -> allKeysAbsent(cachesAndKeys));
    cachesAndKeys.forEach((cache, key) -> assertKeysAbsent(List.of(cache), List.of(key)));
  }

  private <K, V> void assertGetTroughFillsCache(List<Cache<K, V>> caches, Map<K, V> data) {
    caches.forEach(c -> data.forEach((k, v) -> assertEquals(v, c.getThrough(k, () -> v), format("Cache %d", caches.indexOf(c)+1))));
  }

  private <K, V> void assertGetTroughUsesCache(List<Cache<K, V>> caches, Map<K, V> data) {
    caches.forEach(c -> data.forEach((k, v) -> assertEquals(v, c.getThrough(k, Assertions::fail), format("Cache %d", caches.indexOf(c)+1))));
  }

  private <K, V> void assertGetTroughFutureFillsCache(List<Cache<K, V>> caches, Map<K, V> data) {
    caches.forEach(c -> data.forEach((k, v) -> assertEquals(v, await(c.getThroughFuture(k, () -> completedFuture(v))), format("Cache %d", caches.indexOf(c)+1))));
  }

  private <K, V> void assertGetTroughFutureUsesCache(List<Cache<K, V>> caches, Map<K, V> data) {
    caches.forEach(c -> data.forEach((k, v) -> assertEquals(v, await(c.getThroughFuture(k, Assertions::fail)), format("Cache %d", caches.indexOf(c)+1))));
  }

  private <K, V> void assertCacheRefDoesntMatter(List<Cache<K, V>> aCaches, List<Cache<K, V>> bCaches, List<K> keys) {
    IntStream.range(0, aCaches.size()).forEach(i ->
      keys.forEach(k -> assertEquals(aCaches.get(i).get(k), bCaches.get(i).get(k), format("Cache %d", i+1)))
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
      .reduce(false, Boolean::logicalOr);
  }
}