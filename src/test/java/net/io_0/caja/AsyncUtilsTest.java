package net.io_0.caja;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static net.io_0.caja.AsyncUtils.MAX_NANO_DURATION;
import static net.io_0.caja.AsyncUtils.await;
import static org.junit.jupiter.api.Assertions.*;

class AsyncUtilsTest {
  @Test
  void awaitFuture() {
    assertThrows(RuntimeException.class, () -> await(CompletableFuture.failedFuture(new IllegalStateException())));
    assertEquals(DONE, await(CompletableFuture.supplyAsync(sleepySupplier)));

    assertNull(await(CompletableFuture.supplyAsync(sleepySupplier), Duration.ofMillis(5)));
    assertEquals(DONE+DONE, await(CompletableFuture.runAsync(() -> sleepySupplier.get()).thenApply(nothing -> DONE+DONE), Duration.ofMillis(15)));

    assertEquals(DONE, await(CompletableFuture.supplyAsync(sleepySupplier), MAX_NANO_DURATION.plusMillis(10)));
  }

  private static final String DONE = "done";

  private Supplier<String> sleepySupplier = () -> {
    try { Thread.sleep(10 /*ms*/); }
    catch (InterruptedException e) { return null; }
    return DONE;
  };
}