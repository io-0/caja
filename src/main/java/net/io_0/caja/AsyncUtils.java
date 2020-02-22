package net.io_0.caja;

import lombok.extern.slf4j.Slf4j;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.isNull;

@Slf4j
public class AsyncUtils {
  public static final Duration MAX_NANO_DURATION = Duration.ofNanos(Long.MAX_VALUE);

  public static <T> T await(CompletableFuture<T> future) {
    return await(future, null);
  }

  public static <T> T await(CompletableFuture<T> future, Duration timeout) {
    try {
      if (isNull(timeout)) return future.get();
      return timeout.compareTo(MAX_NANO_DURATION) > 0
        ? future.get(timeout.getSeconds(), TimeUnit.SECONDS)
        : future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
    } catch (InterruptedException | TimeoutException e) {
      log.error("failed to await", e);
      return null;
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
