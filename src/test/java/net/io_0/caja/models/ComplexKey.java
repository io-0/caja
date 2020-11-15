package net.io_0.caja.models;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode @ToString
@Getter @Setter
public class ComplexKey {
  private String field1;
  private Integer field2;
  private Nested field3;
}
