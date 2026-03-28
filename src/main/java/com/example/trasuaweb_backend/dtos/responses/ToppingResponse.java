package com.example.trasuaweb_backend.dtos.responses;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToppingResponse {
    private Long id;
    private String name;
    private BigDecimal price;
}
