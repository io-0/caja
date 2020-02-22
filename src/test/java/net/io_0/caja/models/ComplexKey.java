package net.io_0.caja.models;

import lombok.*;

@RequiredArgsConstructor
@EqualsAndHashCode @ToString
@Getter @Setter
public class ComplexKey {
  private final String field1;
  private final Integer field2;
  private final Nested field3;
}
