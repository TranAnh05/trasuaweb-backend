package com.example.trasuaweb_backend.dtos.responses;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long variantId;

    // Lấy từ Snapshot để đảm bảo tính lịch sử
    private String productName;
    private String sizeName;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal; // Giá ly * số lượng (chưa topping)

    private List<OrderItemToppingResponse> toppings;
}
