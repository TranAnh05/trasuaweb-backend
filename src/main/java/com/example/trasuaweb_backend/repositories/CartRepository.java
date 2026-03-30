package com.example.trasuaweb_backend.repositories;

import com.example.trasuaweb_backend.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // 1. Tìm giỏ hàng theo User (Khách đã đăng nhập)
    Optional<Cart> findByUserId(Long userId);

    // 2. Tìm giỏ hàng theo Session (Khách vãng lai)
    Optional<Cart> findBySessionId(String sessionId);
}
