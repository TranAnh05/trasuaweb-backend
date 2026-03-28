package com.example.trasuaweb_backend.configs;

import com.example.trasuaweb_backend.repositories.UserRepository;
import com.example.trasuaweb_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF vì chúng ta làm REST API
                .authorizeHttpRequests(auth -> auth
                        // Cho phép ai cũng vào được các API đăng ký, đăng nhập
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Cho phép xem sản phẩm, menu thoải mái
                        .requestMatchers("/api/v1/products/**", "/api/v1/toppings/**").permitAll()
                        // Còn lại các API khác (sau này làm) thì bắt buộc phải có Token
                        .requestMatchers("/api/v1/users/profile").authenticated()
                        .anyRequest().authenticated()
                )
                // Lệnh này có nghĩa là: Chạy jwtAuthFilter TRƯỚC UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
