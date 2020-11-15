package net.io_0.caja;

import java.util.Collection;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface Asserts {
  static <T> void assertCollectionEquals(Collection<T> a, Collection<T> b) {
    assertTrue(a.size() == b.size() && a.containsAll(b));
  }
}
