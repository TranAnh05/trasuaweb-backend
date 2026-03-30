package com.example.trasuaweb_backend.services;

import com.example.trasuaweb_backend.dtos.requests.CartRequest;
import com.example.trasuaweb_backend.dtos.responses.CartResponse;

public interface ICartService {
    void addToCart(CartRequest request, String userEmail);
    CartResponse getCart(String sessionId, String userEmail);
    // Cập nhật số lượng
    CartResponse updateQuantity(Long cartItemId, Integer quantity, String sessionId, String userEmail);
    // Xóa món khỏi giỏ
    CartResponse removeItem(Long cartItemId, String sessionId, String userEmail);
}
