package com.example.trasuaweb_backend.dtos.responses;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private int status;       // Mã trạng thái (Ví dụ: 200, 400, 500)
    private String message;   // Thông báo cho Frontend (Ví dụ: "Thành công", "Lỗi dữ liệu")
    private T data;           // Dữ liệu thực tế sẽ nằm ở đây
}