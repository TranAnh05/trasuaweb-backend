package com.example.trasuaweb_backend.dtos.responses;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderItemToppingResponse {
    private Long id;
    private String toppingName;
    private Integer quantity;
    private BigDecimal price;
}
