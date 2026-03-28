package com.example.trasuaweb_backend.repositories;

import com.example.trasuaweb_backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm kiếm user theo email (Phục vụ cho lúc Đăng nhập)
    Optional<User> findByEmail(String email);

    // Kiểm tra xem email đã tồn tại chưa (Phục vụ cho lúc Đăng ký)
    boolean existsByEmail(String email);

    // Kiểm tra số điện thoại (Có thể cần dùng sau này)
    boolean existsByPhone(String phone);
}