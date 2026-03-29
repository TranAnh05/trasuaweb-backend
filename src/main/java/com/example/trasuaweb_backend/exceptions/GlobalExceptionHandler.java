package com.example.trasuaweb_backend.exceptions;

import com.example.trasuaweb_backend.dtos.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Hàm này tự động bắt các lỗi do @Valid quăng ra
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Lấy tất cả các lỗi và nhét vào Map (Tên trường : Câu thông báo)
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Trả về JSON chuẩn chỉ cho Frontend
        return ResponseEntity.badRequest().body(
                ApiResponse.<Map<String, String>>builder()
                        .status(400)
                        .message("Dữ liệu đầu vào không hợp lệ")
                        .data(errors)
                        .build()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        // Trả về mã 400 (Bad Request) cùng với câu thông báo lỗi
        return ResponseEntity.badRequest().body(
                ApiResponse.<Object>builder()
                        .status(400) // 400 là mã lỗi do client gửi dữ liệu sai/trùng lặp
                        .message(ex.getMessage()) // Dòng này sẽ moi ra câu "Email này đã được sử dụng!"
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(401).body(
                ApiResponse.<Object>builder()
                        .status(401)
                        .message("Email hoặc mật khẩu không chính xác!")
                        .data(null)
                        .build()
        );
    }

    // (Có thể thêm các Exception khác ở đây sau này như UserNotFoundException...)
}
