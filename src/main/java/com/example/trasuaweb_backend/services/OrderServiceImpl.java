package com.example.trasuaweb_backend.services;

import com.example.trasuaweb_backend.dtos.requests.CheckoutRequest;
import com.example.trasuaweb_backend.dtos.responses.OrderItemResponse;
import com.example.trasuaweb_backend.dtos.responses.OrderItemToppingResponse;
import com.example.trasuaweb_backend.dtos.responses.OrderResponse;
import com.example.trasuaweb_backend.entities.*;
import com.example.trasuaweb_backend.repositories.CartRepository;
import com.example.trasuaweb_backend.repositories.OrderRepository;
import com.example.trasuaweb_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
                .orderItems(new ArrayList<>())
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

        // Tạm thời phí ship = 0, discount = 0
        order.setTotalAmount(totalAmount);

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
}