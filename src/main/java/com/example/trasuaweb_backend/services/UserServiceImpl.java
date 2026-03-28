package com.example.trasuaweb_backend.services;

import com.example.trasuaweb_backend.dtos.requests.LoginRequest;
import com.example.trasuaweb_backend.dtos.requests.RegisterRequest;
import com.example.trasuaweb_backend.dtos.responses.TokenResponse;
import com.example.trasuaweb_backend.dtos.responses.UserResponse;
import com.example.trasuaweb_backend.entities.User;
import com.example.trasuaweb_backend.repositories.UserRepository;
import com.example.trasuaweb_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Công cụ sinh Token
    private final AuthenticationManager authenticationManager; // Công cụ đối chiếu mật khẩu

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 1. Kiểm tra Email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng!");
            // (Thực tế nên tạo CustomException để ném ra)
        }

        // 2. Tạo User mới và BĂM MẬT KHẨU
        User user = User.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // Băm mật khẩu ra mã hóa
                .roles("customer")
                .status("active")
                .emailVerified(false)
                .build();

        // 3. Lưu vào DB
        userRepository.save(user);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        // 1. Xác thực tài khoản và mật khẩu
        // Nếu sai email hoặc mật khẩu, Spring Security sẽ tự động ném ra Exception (403 Forbidden hoặc BadCredentialsException)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Nếu code chạy xuống được đây nghĩa là tài khoản/mật khẩu đã ĐÚNG. Lấy thông tin User ra.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 3. Kiểm tra xem tài khoản có bị khóa không
        if ("banned".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa!");
        }

        // 4. Sinh JWT Token
        String jwtToken = jwtService.generateToken(user);

        // 5. Build DTO trả về cho Frontend
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .status(user.getStatus())
                .build();

        return TokenResponse.builder()
                .token(jwtToken)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String email) {
        // 1. Tìm User trong CSDL
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        // 2. Chuyển đổi Entity sang DTO
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .status(user.getStatus())
                .build();
    }
}
