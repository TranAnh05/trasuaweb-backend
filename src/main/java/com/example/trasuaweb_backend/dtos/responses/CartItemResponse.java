package com.example.trasuaweb_backend.dtos.responses;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartItemResponse {
    private Long id; // ID của dòng CartItem này
    private Long variantId;
    private String productName;
    private String productSlug;
    private String sizeName;
    private String productImage;

    private Integer quantity;
    private BigDecimal basePrice; // Giá 1 ly (priceAtAdd)

    private List<CartToppingResponse> toppings;

    // Tổng tiền của MỘT DÒNG này = (basePrice + tổng tiền topping) * quantity
    private BigDecimal itemTotalPrice;
}
