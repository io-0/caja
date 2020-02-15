package net.io_0.caja;

import java.util.function.Consumer;

public class FnUtils {
  public static <V> V tap(V value, Consumer<V> callback) {
    callback.accept(value);
    return value;
  }
}
