package com.example.trasuaweb_backend.services;

import com.example.trasuaweb_backend.dtos.requests.CheckoutRequest;
import com.example.trasuaweb_backend.dtos.responses.OrderItemResponse;
import com.example.trasuaweb_backend.dtos.responses.OrderItemToppingResponse;
import com.example.trasuaweb_backend.dtos.responses.OrderResponse;
import com.example.trasuaweb_backend.dtos.responses.PageResponse;
import com.example.trasuaweb_backend.entities.*;
import com.example.trasuaweb_backend.repositories.CartRepository;
import com.example.trasuaweb_backend.repositories.OrderRepository;
import com.example.trasuaweb_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderResponse placeOrder(CheckoutRequest request, String userEmail) {
        // 1. LẤY GIỎ HÀNG HIỆN TẠI
        Cart cart = getCartFromIdentity(request.getSessionId(), userEmail);
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang trống hoặc không hợp lệ!");
        }

        BigDecimal shippingFee = new BigDecimal("15000");

        // 2. KHỞI TẠO ĐƠN HÀNG
        Order order = Order.builder()
                .orderNo("ORD-" + System.currentTimeMillis()) // Mã đơn hàng tạm thời
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .internalNote(request.getInternalNote())
                .orderStatus("PENDING")
                .payStatus("UNPAID")
                .shippingFee(shippingFee)
                .orderItems(new ArrayList<>())
                .sessionId(request.getSessionId())
                .build();

        // Nếu là User đã đăng nhập, gắn ID của User vào đơn hàng
        if (userEmail != null) {
            order.setUser(userRepository.findByEmail(userEmail).orElse(null));
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 3. DUYỆT GIỎ HÀNG ĐỂ TẠO CHI TIẾT ĐƠN HÀNG (ORDER ITEMS)
        for (CartItem cartItem : cart.getCartItems()) {
            // [BỔ SUNG] Kiểm tra tồn kho của sản phẩm chính (Kèm check null an toàn)
            if (cartItem.getVariant().getInStock() == null || cartItem.getVariant().getInStock() == 0) {
                throw new RuntimeException("Món " + cartItem.getVariant().getProduct().getName() + " hiện đã tạm hết hàng!");
            }

            // Tạo OrderItem và chụp ảnh (Snapshot) dữ liệu
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .variant(cartItem.getVariant())
                    .productNameSnapshot(cartItem.getVariant().getProduct().getName())
                    .sizeNameSnapshot(cartItem.getVariant().getSize().getName())
                    .unitPrice(cartItem.getPriceAtAdd())
                    .quantity(cartItem.getQuantity())
                    .subtotal(cartItem.getPriceAtAdd().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .toppings(new ArrayList<>())
                    .build();

            // Xử lý Topping cho từng item
            BigDecimal itemToppingTotal = BigDecimal.ZERO;
            if (cartItem.getToppings() != null) {
                for (CartItemTopping cartTopping : cartItem.getToppings()) {

                    // [BỔ SUNG] Kiểm tra tồn kho của Topping
                    if (cartTopping.getTopping().getInStock() == null || !cartTopping.getTopping().getInStock()) {
                        throw new RuntimeException("Topping " + cartTopping.getTopping().getName() + " hiện đã hết hàng, vui lòng điều chỉnh lại giỏ hàng!");
                    }

                    OrderItemTopping orderTopping = OrderItemTopping.builder()
                            .orderItem(orderItem)
                            .topping(cartTopping.getTopping())
                            .toppingNameSnapshot(cartTopping.getTopping().getName())
                            .price(cartTopping.getPriceAtAdd())
                            .quantity(cartTopping.getQuantity())
                            .build();

                    orderItem.getToppings().add(orderTopping);
                    itemToppingTotal = itemToppingTotal.add(cartTopping.getPriceAtAdd().multiply(BigDecimal.valueOf(cartTopping.getQuantity())));
                }
            }

            // Tính tổng tiền = (Giá ly + Tổng giá topping của 1 ly) * Số lượng ly
            BigDecimal lineTotal = (orderItem.getUnitPrice().add(itemToppingTotal)).multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);
            order.getOrderItems().add(orderItem);
        }

        BigDecimal finalTotal = totalAmount.add(shippingFee);
        // Tạm thời discount = 0
        order.setTotalAmount(finalTotal);

        // 4. LƯU ĐƠN HÀNG & XÓA GIỎ HÀNG
        Order savedOrder = orderRepository.save(order);

        // Cực kỳ quan trọng: Nhờ cấu hình CascadeType.ALL, JPA sẽ tự lưu OrderItem và Topping
        // Sau khi lưu thành công, ta mới xóa giỏ hàng
        cartRepository.delete(cart);

        return mapToOrderResponse(savedOrder);
    }

    // ==========================================================
    // HÀM BỔ TRỢ: TÌM GIỎ HÀNG (Dành cho cả Guest và User)
    // ==========================================================
    private Cart getCartFromIdentity(String sessionId, String userEmail) {
        if (userEmail != null) {
            // Xử lý cho Khách đã đăng nhập
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người dùng!"));
            return cartRepository.findByUserId(user.getId()).orElse(null);
        } else {
            // Xử lý cho Khách vãng lai
            if (sessionId == null || sessionId.trim().isEmpty()) {
                throw new RuntimeException("Không tìm thấy phiên làm việc, vui lòng thử lại!");
            }
            return cartRepository.findBySessionId(sessionId).orElse(null);
        }
    }

    // ==========================================================
    // HÀM BỔ TRỢ: MAPPING DTO
    // ==========================================================
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream().map(item -> {
            List<OrderItemToppingResponse> toppingResponses = item.getToppings().stream().map(t ->
                    OrderItemToppingResponse.builder()
                            .id(t.getId())
                            .toppingName(t.getToppingNameSnapshot())
                            .quantity(t.getQuantity())
                            .price(t.getPrice())
                            .build()
            ).toList();

            return OrderItemResponse.builder()
                    .id(item.getId())
                    .variantId(item.getVariant().getId())
                    .productName(item.getProductNameSnapshot())
                    .sizeName(item.getSizeNameSnapshot())
                    .unitPrice(item.getUnitPrice())
                    .quantity(item.getQuantity())
                    .subtotal(item.getSubtotal())
                    .toppings(toppingResponses)
                    .build();
        }).toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .internalNote(order.getInternalNote())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .paymentMethod(order.getPaymentMethod())
                .payStatus(order.getPayStatus())
                .orderStatus(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(String orderNo, String sessionId, String userEmail) {
        // 1. Tìm đơn hàng dưới DB
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderNo));

        // 2. KIỂM TRA BẢO MẬT (CHỐNG LỖI IDOR)
        if (order.getUser() != null) {
            // Đơn hàng này của User đã đăng nhập -> Bắt buộc phải có Token khớp email
            if (userEmail == null || !order.getUser().getEmail().equals(userEmail)) {
                // Trong thực tế nên ném ra AccessDeniedException (Lỗi 403)
                throw new RuntimeException("Truy cập bị từ chối: Bạn không có quyền xem đơn hàng này!");
            }
        } else {
            // Đơn hàng này của Khách vãng lai -> Bắt buộc phải khớp Session ID dưới LocalStorage của họ
            if (sessionId == null || !sessionId.equals(order.getSessionId())) {
                throw new RuntimeException("Truy cập bị từ chối: Phiên làm việc không hợp lệ hoặc đã hết hạn!");
            }
        }

        // 3. Nếu qua được bài kiểm tra, map ra DTO và trả về
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String userEmail) {
        // 1. Tìm User từ Email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // 2. Lấy danh sách đơn hàng của User đó
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        // 3. Map từ Entity sang DTO để trả về
        return orders.stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse trackOrder(String orderNo, String phone) {
        // 1. Tìm đơn hàng bằng cả 2 lớp khóa (Mã đơn + SĐT)
        Order order = orderRepository.findByOrderNoAndCustomerPhone(orderNo, phone)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng hoặc số điện thoại không khớp!"));

        // 2. Chuyển Entity sang DTO
        OrderResponse response = mapToOrderResponse(order);

        // 3. Che mờ dữ liệu nhạy cảm (Data Masking) bảo vệ quyền riêng tư
        response.setShippingAddress(maskAddress(response.getShippingAddress()));

        if (response.getCustomerEmail() != null && !response.getCustomerEmail().isEmpty()) {
            response.setCustomerEmail(maskEmail(response.getCustomerEmail()));
        }

        return response;
    }

    // ==========================================================
    // HÀM BỔ TRỢ: CHE MỜ DỮ LIỆU (DATA MASKING)
    // ==========================================================
    private String maskAddress(String address) {
        if (address == null || address.length() < 10) return "***";
        // Giữ lại 15 ký tự đầu (ví dụ: "123 Đường ABC..."), phần còn lại biến thành dấu *
        return address.substring(0, 15) + " ***";
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) return email;
        // ví dụ: v****h@email.com
        return email.charAt(0) + "****" + email.substring(atIndex - 1);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrdersForAdmin(int page, int size, String status) {
        // 1. Tạo đối tượng Pageable (Trang bắt đầu từ 0 trong Spring Data, nên phải lấy page - 1)
        // Sắp xếp giảm dần theo ngày tạo (Mới nhất lên đầu)
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<Order> orderPage;

        // 2. Kiểm tra xem có yêu cầu lọc theo trạng thái không
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            orderPage = orderRepository.findByOrderStatus(status, pageable);
        } else {
            // Nếu không truyền status hoặc status = "ALL", lấy toàn bộ
            orderPage = orderRepository.findAll(pageable);
        }

        // 3. Chuyển Entity sang DTO
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::mapToOrderResponse) // Gọi lại hàm map cũ em đã viết
                .collect(Collectors.toList());

        // 4. Đóng gói vào PageResponse
        return PageResponse.<OrderResponse>builder()
                .currentPage(page)
                .pageSize(orderPage.getSize())
                .totalPages(orderPage.getTotalPages())
                .totalElements(orderPage.getTotalElements())
                .content(content)
                .build();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String newStatus, String cancelReason) {
        // 1. Tìm đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        String currentStatus = order.getOrderStatus();
        newStatus = newStatus.toUpperCase();

        // 2. Validate logic chuyển trạng thái (State Machine)
        if (currentStatus.equals("COMPLETED") || currentStatus.equals("CANCELLED")) {
            throw new RuntimeException("Đơn hàng đã hoàn tất hoặc đã hủy, không thể thay đổi trạng thái!");
        }

        // Nếu chuyển sang CANCELLED thì bắt buộc phải có lý do
        if (newStatus.equals("CANCELLED")) {
            if (cancelReason == null || cancelReason.trim().isEmpty()) {
                throw new RuntimeException("Vui lòng nhập lý do hủy đơn hàng!");
            }
            order.setCancelReason(cancelReason);
        }

        // 3. Cập nhật trạng thái
        order.setOrderStatus(newStatus);

        // 4. Lưu vào Database
        Order updatedOrder = orderRepository.save(order);

        // 5. Trả về DTO
        return mapToOrderResponse(updatedOrder);
    }
}