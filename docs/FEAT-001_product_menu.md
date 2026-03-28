# TÀI LIỆU ĐẶC TẢ KỸ THUẬT (TECHNICAL SPECIFICATION)
**Tên tính năng:** Danh sách & Tìm kiếm Sản phẩm (Product Menu & Search)

---

## 1. Tổng quan (Overview)
Tính năng Menu Sản phẩm cung cấp giao diện chính để khách hàng duyệt, tìm kiếm và lọc các sản phẩm đồ uống/bánh ngọt của hệ thống R&B Tea.
Mục tiêu là đảm bảo luồng tìm kiếm diễn ra mượt mà, kết quả chính xác, đồng thời tối ưu hóa hiệu suất truy vấn dữ liệu từ Backend để đáp ứng lượng truy cập lớn.

## 2. Phạm vi yêu cầu (Scope & User Stories)
**Người dùng (Khách hàng) có thể:**
1. Xem danh sách toàn bộ sản phẩm được phân trang (Mặc định 12 sản phẩm/trang).
2. Lọc sản phẩm theo danh mục đa cấp (Ví dụ: Trà sữa -> Trà sữa kem cheese) thông qua thanh Sidebar.
3. Tìm kiếm sản phẩm theo từ khóa (Tên sản phẩm) thông qua thanh Search Bar.
4. Sắp xếp sản phẩm theo các tiêu chí: Mới nhất, Phổ biến, Giá tăng dần, Giá giảm dần. #Chưa hoàn thành xong
5. Sao chép đường dẫn (URL) hiện tại gửi cho người khác và giữ nguyên các tiêu chí đang lọc.

## 3. Thiết kế luồng nghiệp vụ (Business Logic Flow)

* **Luồng Tìm kiếm (Smart Search/Filter):**
    * Khi người dùng chọn một Danh mục ở Sidebar -> Hệ thống tự động xóa Từ khóa tìm kiếm hiện tại để tránh xung đột điều kiện `AND` -> Hiển thị danh sách sản phẩm thuộc danh mục đó (bao gồm cả danh mục con).
    * Khi người dùng gõ Từ khóa và Enter -> Hệ thống ưu tiên tìm kiếm chuỗi trên toàn hệ thống -> Tự động xóa bộ lọc Danh mục đang chọn (trở về trạng thái "Tất cả món").
    * Mọi thay đổi về bộ lọc (Danh mục, Từ khóa, Sắp xếp) đều tự động đưa người dùng trở về `Page 1`.

## 4. Thiết kế Kỹ thuật (Technical Architecture)

### 4.1. Backend (Spring Boot)
* **API Lấy Danh Mục (`GET /api/v1/categories`)**
    * **Giải pháp:** Xử lý bài toán danh mục đa cấp (Nested Category).
    * **Kỹ thuật:** Sử dụng `Collectors.groupingBy` (Java 8 Stream) để gom nhóm danh mục Cha - Con trên RAM. Triệt tiêu hoàn toàn lỗi **N+1 Query**, chỉ tốn đúng 1 lượt truy vấn xuống Database.
* **API Danh sách Sản phẩm (`GET /api/v1/products`)**
    * **Giải pháp:** Lọc động (Dynamic Filtering) và tính toán khoảng giá (Price Range).
    * **Kỹ thuật:** Sử dụng `@Query` JPQL tích hợp `Pageable`. Câu truy vấn tự động bỏ qua các tham số rỗng/null và quét đệ quy các sản phẩm nằm trong danh mục con thông qua phép `IN (subquery)`. Tính toán Min/Max price bằng Stream API từ danh sách `ProductVariant`.
    * **Bảo mật dữ liệu:** Sử dụng `BigDecimal` cho toàn bộ logic tiền tệ; Dùng DTO (`ProductResponse`) để ẩn các trường hệ thống không cần thiết.

### 4.2. Frontend (ReactJS)
* **Quản lý trạng thái (State Management):**
    * Chuyển đổi toàn bộ Local State (`useState`) sang URL Parameters (`useSearchParams` của `react-router-dom`). Việc này giúp đồng bộ hoàn toàn UI với URL (URL-Driven State).
* **UI/UX:**
    * Áp dụng Tailwind CSS để thiết kế Responsive (Mobile-first).
    * Tích hợp Empty State (Giao diện khi không có dữ liệu) và Loading Indicator để tăng trải nghiệm người dùng.

## 5. Tiêu chí nghiệm thu (Acceptance Criteria / Test Cases)
Đội ngũ QA/Thành viên nhóm vui lòng kiểm tra theo các kịch bản sau:

| STT | Kịch bản kiểm thử (Test Case) | Kết quả mong đợi (Expected Result) |
|---|---|---|
| 1 | Truy cập `/menu` không có tham số | Hiển thị toàn bộ sản phẩm, trang 1, mới nhất. |
| 2 | Click chọn danh mục "Trà sữa" | Hiển thị sản phẩm của Trà sữa và các danh mục con của nó. Ô Search bị xóa trắng. |
| 3 | Gõ từ khóa "bánh" vào ô tìm kiếm và Enter | Danh sách hiển thị các loại bánh. Sidebar tự động nhảy về mục "Tất cả". |
| 4 | Tìm một từ khóa vô lý (vd: "asdasd") | Hiển thị giao diện "Không tìm thấy món này". |
| 5 | Copy URL `/menu?category=tra-sua&page=2` mở ở tab ẩn danh | Load đúng trang 2 của danh mục Trà sữa. |

---