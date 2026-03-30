package com.example.trasuaweb_backend.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CartRequest {
    @NotNull(message = "Thiếu thông tin sản phẩm (Variant ID)")
    private Long variantId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    // session_id dùng cho khách vãng lai. Nếu khách đã đăng nhập thì React có thể gửi null.
    private String sessionId;

    // Danh sách topping khách chọn (Có thể rỗng hoặc null nếu khách uống nguyên bản)
    private List<CartToppingRequest> toppings;
}