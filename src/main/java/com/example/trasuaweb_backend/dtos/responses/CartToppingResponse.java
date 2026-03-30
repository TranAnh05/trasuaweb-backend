package com.example.trasuaweb_backend.dtos.responses;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CartToppingResponse {
    private Long id; // ID của CartItemTopping
    private Long toppingId;
    private String name;
    private Integer quantity;
    private BigDecimal price; // Lấy từ priceAtAdd
    private BigDecimal totalPrice; // price * quantity
}
