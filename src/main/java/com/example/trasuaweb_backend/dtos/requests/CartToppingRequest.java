package com.example.trasuaweb_backend.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartToppingRequest {
    @NotNull(message = "Thiếu ID của topping")
    private Long toppingId;

    @NotNull(message = "Số lượng topping không được để trống")
    @Min(value = 1, message = "Số lượng topping phải lớn hơn 0")
    private Integer quantity;
}
