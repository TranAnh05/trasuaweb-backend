# 📄 TÀI LIỆU ĐẶC TẢ KỸ THUẬT (TECHNICAL SPECIFICATION)

**Tên tính năng:** Xác thực Người dùng (Authentication) 
**Mã tính năng:** FEAT-003

---

## 1. Tổng quan (Overview)
Module Xác thực là lớp bảo mật cốt lõi của hệ thống E-commerce R&B Tea. Hệ thống áp dụng kiến trúc **Stateless (Không lưu trạng thái)** bằng cách sử dụng **JSON Web Token (JWT)** thay cho `HttpSession` truyền thống. Giải pháp này đảm bảo tính bảo mật cao, khả năng mở rộng (Scaling) tốt khi triển khai Microservices, và tương thích hoàn toàn với nền tảng Frontend (ReactJS) độc lập.

## 2. Giải pháp Kỹ thuật Cốt lõi (Core Solutions)

### 2.1. Giải pháp Xác thực (Authentication)
* **Công nghệ:** Spring Security 6 + JJWT (JSON Web Token).
* **Mã hóa:** Mật khẩu người dùng được băm (hash) một chiều bằng thuật toán `BCrypt` trước khi lưu vào cơ sở dữ liệu. Tuyệt đối không lưu plaintext.
* **Cơ chế Token:**
    * Token được sinh ra từ Backend và ký bằng `SECRET_KEY` (HS256).
    * Payload của Token chứa các `claims` không nhạy cảm: `sub` (Email), ngày cấp (IAT), và ngày hết hạn (EXP - 24 giờ).
    * Frontend (React) chịu trách nhiệm lưu trữ Token tại `LocalStorage` và đính kèm vào HTTP Header (`Authorization: Bearer <token>`) trong các request yêu cầu bảo mật.
---

## 3. Đặc tả Luồng Nghiệp vụ

### 3.1. Luồng Đăng ký Tài khoản (Registration Flow)
1. **Client Request:** Người dùng gửi Payload (Họ tên, SĐT, Email, Mật khẩu) thông qua API `POST /api/v1/auth/register`.
2. **Validation:** Backend kiểm tra tính hợp lệ của dữ liệu (Format Email, độ dài chuỗi...). Nếu lỗi, trả về HTTP 400 (Bad Request).
3. **Kiểm tra Trùng lặp:** Quét bảng `users` để đảm bảo Email chưa tồn tại.
4. **Mã hóa:** Sử dụng `BCryptPasswordEncoder` để băm mật khẩu.
5. **Lưu trữ:** Insert bản ghi mới vào Database với quyền mặc định `roles = "customer"` và trạng thái `status = "active"`.
6. **Response:** Trả về HTTP 201 (Created) cho Frontend.

### 3.2. Luồng Đăng nhập (Login Flow)
1. **Client Request:** Người dùng gửi Payload (Email, Mật khẩu) thông qua API `POST /api/v1/auth/login`.
2. **Authenticate:** Spring Security `AuthenticationManager` tìm kiếm User theo Email.
3. **Verify Password:** Đối chiếu mật khẩu nhập vào với mã băm `password_hash` trong CSDL. Nếu sai, ném ngoại lệ (HTTP 401).
4. **Check Status:** Kiểm tra trường `status`. Từ chối đăng nhập nếu tài khoản bị khóa (`banned`).
5. **Generate JWT:** `JwtService` khởi tạo Token có thời hạn 24 giờ.
6. **Response:** Trả về HTTP 200 (OK) kèm cặp dữ liệu `{ token, user_info }`.
7. **Client Store:** Frontend lưu Token vào `LocalStorage` để sử dụng cho các API tiếp theo.
