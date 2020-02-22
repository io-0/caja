package net.io_0.caja.models;

import lombok.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode @ToString
@Getter @Setter
public class Nested {
  private Boolean field1;
  private List<Integer> field2;
}
