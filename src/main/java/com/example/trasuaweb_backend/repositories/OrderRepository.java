package com.example.trasuaweb_backend.repositories;

import com.example.trasuaweb_backend.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Có thể lấy danh sách đơn hàng của một user
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
}
