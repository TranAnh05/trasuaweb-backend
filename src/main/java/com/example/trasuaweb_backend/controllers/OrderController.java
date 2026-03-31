package com.example.trasuaweb_backend.controllers;

import com.example.trasuaweb_backend.dtos.requests.CheckoutRequest;
import com.example.trasuaweb_backend.dtos.responses.ApiResponse;
import com.example.trasuaweb_backend.dtos.responses.OrderResponse;
import com.example.trasuaweb_backend.services.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    /**
     * API Tiến hành đặt hàng (Checkout)
     * Hỗ trợ cả Khách vãng lai (qua sessionId trong body) và User (qua Token)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 1. Kiểm tra xem người dùng đã đăng nhập hay chưa (Lấy email từ Token)
        String userEmail = userDetails != null ? userDetails.getUsername() : null;

        // 2. Chuyển thông tin xuống Service để thực hiện "Chốt đơn"
        OrderResponse orderResponse = orderService.placeOrder(request, userEmail);

        // 3. Trả về kết quả cho Frontend
        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .status(200)
                        .message("Đặt hàng thành công!")
                        .data(orderResponse)
                        .build()
        );
    }

    // =======================================================
    // DỰ KIẾN CÁC API TIẾP THEO SẼ ĐƯỢC THÊM VÀO ĐÂY:
    // =======================================================
    // @GetMapping("/{orderNo}") -> Lấy chi tiết 1 đơn hàng để hiển thị trang Cảm ơn
    // @GetMapping("/my-orders") -> Lấy danh sách lịch sử mua hàng của User
    // @PutMapping("/{orderNo}/cancel") -> Khách hàng tự hủy đơn khi đang chờ xác nhận
}
