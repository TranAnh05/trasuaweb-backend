package com.example.trasuaweb_backend.exceptions;

import com.example.trasuaweb_backend.dtos.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
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

    // (Có thể thêm các Exception khác ở đây sau này như UserNotFoundException...)
}
