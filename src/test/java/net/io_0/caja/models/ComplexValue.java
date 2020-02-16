package net.io_0.caja.models;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode @ToString
@Getter @Setter
public class ComplexValue {
  private Long field1;
  private BigDecimal field2;
  private LocalDateTime field3;
  private Nested field4;
}
