package com.example.trasuaweb_backend.repositories;

import com.example.trasuaweb_backend.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Tìm đơn hàng bằng mã OrderNo (VD: ORD-12345)
    Optional<Order> findByOrderNo(String orderNo);

    // Lấy danh sách đơn hàng của User (Dùng cho tab Lịch sử mua hàng sau này)
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    // Tìm đơn hàng khớp chính xác cả Mã đơn và Số điện thoại
    Optional<Order> findByOrderNoAndCustomerPhone(String orderNo, String customerPhone);
}
