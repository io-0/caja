# Caja

## About
**Ca**ches **Ja**va Objects, local or remote (via Redis).

## Usage
### Import
Use Maven or Gradle, e.g. add repository and dependency to `build.gradle`:
```Gradle
repositories {
  ...
  maven { url "https://jitpack.io" }
  ...
}
```
```Gradle
dependencies {
  ...
  implementation "com.github.io-0:caja:develop-SNAPSHOT"
  ...
}
```
### Grab a cache manager
A Cache Manager is the first step to acquire a cache. It holds all configurations beside types.
```Java
// All caches retrieved from this will be local caches with heap 100 items and ttl 1 second.
CacheManager cacheManager = new CacheManager();
```
```Java
// All caches retrieved from this will be local caches with heap 10 items and ttl 2 second.
CacheManager cacheManager = new CacheManager(new LocalCacheConfig().setHeap(10).setTtlInSeconds(2));
```
```Java
// All caches retrieved from this will be local caches with heap 100 items and ttl 1 second, with one exception:
// If the cache is called 'my cache', it's heap will be 5 items and it's ttl 2 seconds.
CacheManager cacheManager = new CacheManager(new CacheManagerConfig()
  .setCacheConfigurations(Map.of("my cache", new LocalCacheConfig().setHeap(5).setTtlInSeconds(2)))
);
```
```Java
// All caches retrieved from this will be remote caches with ttl 2 second.
CacheManager cacheManager = new CacheManager(
  new RemoteCacheConfig().setTtlInSeconds(2).setHost("redis://localhost:6379/0")
);
```
```Java
// All caches retrieved from this will be remote caches with ttl 1 second, with one exception:
// If the cache is called 'my cache', it will be remote with ttl 2 seconds.
CacheManager cacheManager = new CacheManager(new CacheManagerConfig()
  .setDefaultCacheConfiguration(new RemoteCacheConfig().setHost("redis://localhost:6379/0"))
  .setCacheConfigurations(Map.of("my cache", new RemoteCacheConfig()
    .setTtlInSeconds(2).setHost("redis://localhost:6379/0")
  ))
);
```
### Get a cache and use it
The second step is type binding. Each cache has a name and types for keys and values. For this example we choose simple types, but it is also possible to use more complex types (e.g. POJOs).
```Java
Cache<Integer, String> shortTermCache = 
  cacheManager.getAsSync("short term cache", Integer.class, String.class);

System.out.println(shortTermCache.containsKey(1)); // prints false
shortTermCache.put(2, "two");
System.out.println(shortTermCache.get(2)); // prints 'two'
System.out.println(shortTermCache.getThrough(2, () -> "three")); // prints 'two', after
// ttl expires it would print 'three' and populate the cache
```
It is also possible to interact with caches in an async manner.
```Java
Cache<Integer, String> shortTermCacheAsync = 
  cacheManager.getAsAsync("short term cache", Integer.class, String.class);

System.out.println(await(shortTermCacheAsync.get(2))); // prints 'two', same 
// cache with async interface
```

Remote caches require a running redis instance.
For testing this can be easily archived with e.g. docker:

`docker run --name redis -p 6379:6379 -d redis`
(from [redis@dockerhub](https://hub.docker.com/_/redis/)) 

### Cache configuration
If no configuration is given, the CacheManager defaults to LocalCacheConfig for all caches.
If a LocalCacheConfig or RemoteCacheConfig is provided via CacheManager constructor, it is set as default config and used for all caches.
One can overwrite default config by providing name specific configuration via CacheManagerConfig in CacheManager constructor.
One can overwrite default config by providing a new default as cacheManager.getAs* parameter. This parameter will be ignored if a matching named config exists.

#### Configuration priority
1. CacheManager constructor CacheManagerConfig matching named CacheConfig (optional)
2. cacheManager.getAs* parameter CacheConfig (optional)
3. CacheManager constructor CacheManagerConfig DefaultCacheConfig (optional)
4. LocalCacheConfig

#### LocalCacheConfig
* ttlInSeconds (default 1): Time to live for all cached items in seconds
* logStatistics (default LogLevel.DEBUG): Log cache hits, misses, etc. at configured level
* heap (default 100): Number of maximum items cached

#### RemoteCacheConfig
* ttlInSeconds (default 1): Time to live for all cached items in seconds
* logStatistics (default LogLevel.DEBUG): Log cache hits, misses, etc. at configured level
* host (default "redis://localhost:6379/0"): Location of remote cache