package com.example.trasuaweb_backend.dtos.responses;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderNo;

    // Thông tin khách hàng & Giao hàng
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private String internalNote;

    // Thông tin thanh toán & Trạng thái
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private String paymentMethod;
    private String payStatus;
    private String orderStatus;

    // Thời gian đặt hàng
    private LocalDateTime createdAt;

    // Danh sách món đã đặt
    private List<OrderItemResponse> items;
}
