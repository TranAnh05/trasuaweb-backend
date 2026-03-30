package com.example.trasuaweb_backend.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemRequest {
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng ít nhất phải là 1 ly")
    @Max(value = 20, message = "Chỉ được mua tối đa 20 ly cho mỗi món")
    private Integer quantity;
}

