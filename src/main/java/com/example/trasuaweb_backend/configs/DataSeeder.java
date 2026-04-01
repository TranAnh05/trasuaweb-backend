package com.example.trasuaweb_backend.configs;

import com.example.trasuaweb_backend.entities.User;
import com.example.trasuaweb_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra xem email admin mặc định đã tồn tại chưa
        if (!userRepository.existsByEmail("admin@trasua.com")) {

            User admin = User.builder()
                    .fullName("Quản trị viên")
                    .email("admin@trasua.com")
                    .passwordHash(passwordEncoder.encode("admin123456")) // Mật khẩu mặc định
                    .phone("0999999999")
                    .roles("admin")
                    .build();

            userRepository.save(admin);
            System.out.println("Đã khởi tạo tài khoản Admin mặc định: admin@trasua.com / admin123456");
        }
    }
}