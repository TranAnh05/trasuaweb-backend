package com.example.trasuaweb_backend.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // 1. Chỉ áp dụng CORS cho các route API
                // 2. Chỉ định rõ domain của Frontend (React thường chạy port 3000 hoặc Vite port 5173)
                .allowedOrigins(
                        "http://localhost:3000", // Cổng của Client
                        "http://localhost:3001"  // Cổng của Admin
                )
                // 3. Các HTTP Methods được phép đi qua
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 4. Cho phép Frontend gửi lên các Header tùy chỉnh (rất quan trọng khi gửi JWT Token)
                .allowedHeaders("Authorization", "Content-Type", "Accept")
                // 5. Bắt buộc là true nếu API có sử dụng Session/Cookie hoặc xác thực
                .allowCredentials(true)
                // 6. Cache kết quả preflight request (OPTIONS) trong 1 giờ để giảm tải cho server
                .maxAge(3600);
    }
}
