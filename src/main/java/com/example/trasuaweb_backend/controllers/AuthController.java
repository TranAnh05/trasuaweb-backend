package com.example.trasuaweb_backend.controllers;


import com.example.trasuaweb_backend.dtos.requests.LoginRequest;
import com.example.trasuaweb_backend.dtos.requests.RegisterRequest;
import com.example.trasuaweb_backend.dtos.responses.ApiResponse;
import com.example.trasuaweb_backend.services.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(201).body(
                ApiResponse.<String>builder()
                        .status(201)
                        .message("Đăng ký tài khoản thành công")
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@Valid @RequestBody LoginRequest request) {
        // Tạm thời trả về Object, sau này JWT làm xong sẽ trả về TokenResponse
        Object loginData = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.<Object>builder()
                        .status(200)
                        .message("Đăng nhập thành công")
                        .data(loginData)
                        .build()
        );
    }
}
