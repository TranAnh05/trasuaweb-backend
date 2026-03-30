package com.example.trasuaweb_backend.controllers;

import com.example.trasuaweb_backend.dtos.requests.CartRequest;
import com.example.trasuaweb_backend.dtos.requests.UpdateCartItemRequest;
import com.example.trasuaweb_backend.dtos.responses.ApiResponse;
import com.example.trasuaweb_backend.dtos.responses.CartResponse;
import com.example.trasuaweb_backend.services.ICartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addToCart(
            @Valid @RequestBody CartRequest request,
            // Spring Security tự động tiêm thông tin user đang đăng nhập vào đây (nếu có Token)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = null;

        // Kiểm tra xem khách có đang đăng nhập không
        if (userDetails != null) {
            userEmail = userDetails.getUsername(); // Lấy email của khách
        } else if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            // Nếu không đăng nhập mà cũng không gửi sessionId thì báo lỗi ngay
            throw new RuntimeException("Thiếu mã phiên hoạt động (Session ID) cho khách vãng lai!");
        }

        // Gọi Service xử lý logic
        cartService.addToCart(request, userEmail);

        // Trả về kết quả thành công
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message("Thêm vào giỏ hàng thành công!")
                        .data(null)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestParam(required = false) String sessionId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = null;

        if (userDetails != null) {
            userEmail = userDetails.getUsername();
        } else if (sessionId == null || sessionId.trim().isEmpty()) {
            // Nếu không có cả Token lẫn SessionId, coi như giỏ hàng rỗng
            return ResponseEntity.ok(
                    ApiResponse.<CartResponse>builder()
                            .status(200)
                            .message("Giỏ hàng rỗng")
                            .data(CartResponse.builder()
                                    .cartItems(new ArrayList<>())
                                    .totalItems(0)
                                    .cartTotalPrice(BigDecimal.ZERO)
                                    .build())
                            .build()
            );
        }

        CartResponse cartResponse = cartService.getCart(sessionId, userEmail);

        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .status(200)
                        .message("Lấy giỏ hàng thành công")
                        .data(cartResponse)
                        .build()
        );
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @RequestParam(required = false) String sessionId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails != null ? userDetails.getUsername() : null;

        CartResponse cartResponse = cartService.updateQuantity(itemId, request.getQuantity(), sessionId, userEmail);

        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .status(200)
                        .message("Cập nhật số lượng thành công")
                        .data(cartResponse)
                        .build()
        );
    }

    // API Xóa món khỏi giỏ
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long itemId,
            @RequestParam(required = false) String sessionId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails != null ? userDetails.getUsername() : null;

        CartResponse cartResponse = cartService.removeItem(itemId, sessionId, userEmail);

        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .status(200)
                        .message("Xóa món thành công")
                        .data(cartResponse)
                        .build()
        );
    }
}
