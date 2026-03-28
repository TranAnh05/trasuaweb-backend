package com.example.trasuaweb_backend.services;

import com.example.trasuaweb_backend.dtos.requests.LoginRequest;
import com.example.trasuaweb_backend.dtos.requests.RegisterRequest;
import com.example.trasuaweb_backend.dtos.responses.TokenResponse;
import com.example.trasuaweb_backend.dtos.responses.UserResponse;

public interface IUserService {
    // Lấy thông tin hồ sơ của người dùng (Sẽ dùng khi họ vào trang Profile)
    UserResponse getUserProfile(String email);
    void register(RegisterRequest request);
    TokenResponse login(LoginRequest request);

    // (Sau này em có thể thêm các hàm như updateUserProfile, changePassword... ở đây)
}
