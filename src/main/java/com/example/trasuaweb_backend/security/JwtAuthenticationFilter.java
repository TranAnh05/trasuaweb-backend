package com.example.trasuaweb_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Lấy chuỗi Token từ HTTP Header có tên là "Authorization"
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Kiểm tra xem header có tồn tại và có bắt đầu bằng chữ "Bearer " không
        // (Đây là chuẩn quốc tế, Frontend phải gửi lên dạng: Bearer eyJhbGci...)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Không có thẻ thì cho đi tiếp (sẽ bị Spring chặn lại ở cửa sau nếu API đó yêu cầu bảo mật)
            return;
        }

        // 3. Cắt lấy Token thật (bỏ 7 ký tự "Bearer " ở đầu đi)
        jwt = authHeader.substring(7);

        // 4. Gọi JwtService để giải mã lấy Email từ Token
        userEmail = jwtService.extractUsername(jwt);

        // 5. Nếu có Email và SecurityContext chưa có thông tin đăng nhập của phiên này
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Lấy thông tin User từ Database lên
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 6. Kiểm tra xem Token có hợp lệ và khớp với User này không
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 7. Tạo một "Giấy chứng nhận" đăng nhập hợp lệ
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 8. Đưa "Giấy chứng nhận" này vào bộ nhớ SecurityContext của Spring
                // (Từ lúc này, Spring Boot chính thức công nhận User này đã đăng nhập thành công)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 9. Chuyển request cho đi tiếp đến Controller
        filterChain.doFilter(request, response);
    }
}
