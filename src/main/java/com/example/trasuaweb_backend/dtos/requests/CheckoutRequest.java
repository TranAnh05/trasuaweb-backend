package com.example.trasuaweb_backend.dtos.requests;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotBlank(message = "Họ tên người nhận không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    // Regex chuẩn cho số điện thoại Việt Nam (Bắt đầu bằng 0 hoặc +84, theo sau là 9 số)
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không hợp lệ (Vui lòng nhập số điện thoại Việt Nam)")
    private String customerPhone;

    // Email không bắt buộc nhập (có thể null), nhưng nếu nhập thì phải đúng định dạng
    @Email(message = "Email không đúng định dạng")
    private String customerEmail;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(min = 10, max = 500, message = "Vui lòng nhập địa chỉ giao hàng chi tiết hơn")
    private String shippingAddress;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    @Pattern(regexp = "^(COD|VNPAY|MOMO)$", message = "Phương thức thanh toán không được hỗ trợ")
    private String paymentMethod;

    // Ghi chú và SessionId không bắt buộc nên không cần đánh dấu @NotBlank
    private String internalNote;

    private String sessionId;
}
