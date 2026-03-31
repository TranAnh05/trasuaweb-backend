package com.example.trasuaweb_backend.services;

import com.example.trasuaweb_backend.dtos.requests.CartRequest;
import com.example.trasuaweb_backend.dtos.requests.CartToppingRequest;
import com.example.trasuaweb_backend.dtos.responses.CartItemResponse;
import com.example.trasuaweb_backend.dtos.responses.CartResponse;
import com.example.trasuaweb_backend.dtos.responses.CartToppingResponse;
import com.example.trasuaweb_backend.entities.*;
import com.example.trasuaweb_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService{
    private final CartRepository cartRepository;
    private final ProductVariantRepository variantRepository;
    private final ToppingRepository toppingRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public void addToCart(CartRequest request, String userEmail) {
        // 1. LẤY HOẶC TẠO GIỎ HÀNG (Dựa vào việc có Đăng nhập hay chưa)
        Cart cart = getOrCreateCart(request.getSessionId(), userEmail);

        // 2. KIỂM TRA SẢN PHẨM TỒN TẠI & CÒN HÀNG
        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        if (variant.getInStock() == null || variant.getInStock() == 0) {
            throw new RuntimeException("Xin lỗi bạn, sản phẩm này hiện đang tạm hết hàng!");
        }

        if (cart.getCartItems() == null) {
            cart.setCartItems(new ArrayList<>());
        }

        // 3. XỬ LÝ LOGIC THÊM MÓN VÀO GIỎ
        // Tìm xem trong giỏ đã có món nào Y HỆT (Trùng Variant + Trùng Topping) chưa
        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(item -> isMatchingCartItem(item, request))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            // NẾU ĐÃ CÓ MÓN Y HỆT -> CHỈ CẦN CỘNG DỒN SỐ LƯỢNG
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            // NẾU LÀ MÓN MỚI (HOẶC MÓN CŨ NHƯNG KHÁC TOPPING) -> TẠO DÒNG MỚI
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .priceAtAdd(variant.getPrice()) // Chốt giá trà sữa ngay lúc thêm
                    .toppings(new ArrayList<>())
                    .build();

            // Nếu khách có chọn topping thì xử lý topping
            if (request.getToppings() != null && !request.getToppings().isEmpty()) {
                for (CartToppingRequest toppingReq : request.getToppings()) {
                    Topping topping = toppingRepository.findById(toppingReq.getToppingId())
                            .orElseThrow(() -> new RuntimeException("Topping không tồn tại!"));

                    if (!topping.getInStock() || !"active".equals(topping.getStatus())) {
                        throw new RuntimeException("Topping " + topping.getName() + " hiện không khả dụng!");
                    }

                    CartItemTopping cartItemTopping = CartItemTopping.builder()
                            .cartItem(newItem)
                            .topping(topping)
                            .quantity(toppingReq.getQuantity())
                            .priceAtAdd(topping.getPrice()) // Chốt giá topping
                            .build();

                    newItem.getToppings().add(cartItemTopping);
                }
            }
            cart.getCartItems().add(newItem);
        }

        // 4. LƯU VÀO DATABASE (JPA sẽ tự động lưu cả Cart, CartItem và CartItemTopping nhờ CascadeType.ALL)
        cartRepository.save(cart);
    }

    // Thuật toán kiểm tra 2 món có giống y hệt nhau không
    private boolean isMatchingCartItem(CartItem item, CartRequest request) {
        // 1. Phải trùng Variant (Ví dụ: Cùng là Trà Đào Size L)
        if (!item.getVariant().getId().equals(request.getVariantId())) {
            return false;
        }

        // 2. So sánh danh sách Topping
        List<CartToppingRequest> requestToppings = request.getToppings() == null ? new ArrayList<>() : request.getToppings();
        List<CartItemTopping> itemToppings = item.getToppings() == null ? new ArrayList<>() : item.getToppings();

        // Nếu số lượng loại topping khác nhau -> Chắc chắn khác nhau
        if (requestToppings.size() != itemToppings.size()) {
            return false;
        }

        // Nếu số lượng loại bằng nhau, kiểm tra chi tiết từng cái xem có khớp ID và số lượng không
        for (CartToppingRequest reqTopping : requestToppings) {
            boolean hasMatch = itemToppings.stream().anyMatch(t ->
                    t.getTopping().getId().equals(reqTopping.getToppingId()) &&
                            t.getQuantity().equals(reqTopping.getQuantity())
            );
            if (!hasMatch) return false;
        }

        return true;
    }

    private boolean isMatchingCartItem(CartItem item1, CartItem item2) {
        if (!item1.getVariant().getId().equals(item2.getVariant().getId())) {
            return false;
        }

        List<CartItemTopping> top1 = item1.getToppings() == null ? new ArrayList<>() : item1.getToppings();
        List<CartItemTopping> top2 = item2.getToppings() == null ? new ArrayList<>() : item2.getToppings();

        if (top1.size() != top2.size()) return false;

        for (CartItemTopping t2 : top2) {
            boolean hasMatch = top1.stream().anyMatch(t1 ->
                    t1.getTopping().getId().equals(t2.getTopping().getId()) &&
                            t1.getQuantity().equals(t2.getQuantity())
            );
            if (!hasMatch) return false;
        }
        return true;
    }

    // Hàm lấy giỏ hàng hiện tại hoặc tạo mới nếu chưa có
    private Cart getOrCreateCart(String sessionId, String userEmail) {
        if (userEmail != null) {
            // Khách ĐÃ ĐĂNG NHẬP
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));
            return cartRepository.findByUserId(user.getId())
                    .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
        } else {
            // Khách VÃNG LAI
            if (sessionId == null || sessionId.trim().isEmpty()) {
                throw new RuntimeException("Thiếu Session ID cho khách vãng lai!");
            }
            return cartRepository.findBySessionId(sessionId)
                    .orElseGet(() -> cartRepository.save(Cart.builder().sessionId(sessionId).build()));
        }
    }

    @Transactional
    protected void mergeCart(String sessionId, User user) {
        // 1. Tìm giỏ hàng của Khách vãng lai
        Optional<Cart> guestCartOpt = cartRepository.findBySessionId(sessionId);
        if (guestCartOpt.isEmpty()) return; // Không có giỏ vãng lai thì thôi

        Cart guestCart = guestCartOpt.get();
        if (guestCart.getCartItems() == null || guestCart.getCartItems().isEmpty()) {
            // Giỏ vãng lai rỗng -> Xóa luôn cho sạch Database
            cartRepository.delete(guestCart);
            return;
        }

        // 2. Lấy hoặc tạo giỏ hàng chính thức của User
        Cart userCart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        if (userCart.getCartItems() == null) {
            userCart.setCartItems(new ArrayList<>());
        }

        // 3. Tiến hành chuyển đồ từ Giỏ Guest sang Giỏ User
        for (CartItem guestItem : guestCart.getCartItems()) {
            Optional<CartItem> existingItemOpt = userCart.getCartItems().stream()
                    .filter(item -> isMatchingCartItem(item, guestItem))
                    .findFirst();

            if (existingItemOpt.isPresent()) {
                // NẾU TRÙNG: Cộng dồn số lượng
                CartItem existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + guestItem.getQuantity());
            } else {
                // NẾU CHƯA CÓ: Đổi chủ sở hữu của món hàng sang User Cart
                guestItem.setCart(userCart);
                userCart.getCartItems().add(guestItem);
            }
        }

        // 4. [TRÁNH BẪY HIBERNATE]: Phải bứt các món hàng ra khỏi Giỏ Guest trước khi xóa Giỏ Guest.
        // Nếu không làm bước này, khi xóa Giỏ Guest, Hibernate sẽ xóa sạch luôn các món hàng vừa chuyển đi!
        guestCart.getCartItems().clear();

        // 5. Lưu Giỏ User và Xóa Giỏ Guest
        cartRepository.save(userCart);
        cartRepository.delete(guestCart);
    }

    @Override
    @Transactional
    public CartResponse getCart(String sessionId, String userEmail) {
        Cart cart = null;

        // 1. Tìm giỏ hàng
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElseThrow();
            // Nếu Frontend gửi lên cả Token (có userEmail) VÀ có sessionId -> Chạy thuật toán gộp!
            if (sessionId != null && !sessionId.trim().isEmpty()) {
                mergeCart(sessionId, user);
            }
            cart = cartRepository.findByUserId(user.getId()).orElse(null);
        } else {
            cart = cartRepository.findBySessionId(sessionId).orElse(null);
        }

        // 2. Nếu khách chưa từng thêm gì vào giỏ -> Trả về giỏ hàng rỗng
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return CartResponse.builder()
                    .cartItems(new ArrayList<>())
                    .totalItems(0)
                    .cartTotalPrice(BigDecimal.ZERO)
                    .build();
        }

        // 3. Map dữ liệu từ Entity sang DTO và tính tiền
        int totalItems = 0;
        BigDecimal cartTotalPrice = BigDecimal.ZERO;
        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItem item : cart.getCartItems()) {
            // Đếm tổng số ly
            totalItems += item.getQuantity();

            // Tính tiền Topping
            BigDecimal toppingsTotal = BigDecimal.ZERO;
            List<CartToppingResponse> toppingResponses = new ArrayList<>();

            if (item.getToppings() != null) {
                for (CartItemTopping t : item.getToppings()) {
                    BigDecimal tPrice = t.getPriceAtAdd();
                    BigDecimal tTotal = tPrice.multiply(BigDecimal.valueOf(t.getQuantity()));
                    toppingsTotal = toppingsTotal.add(tTotal);

                    toppingResponses.add(CartToppingResponse.builder()
                            .id(t.getId())
                            .toppingId(t.getTopping().getId())
                            .name(t.getTopping().getName())
                            .quantity(t.getQuantity())
                            .price(tPrice)
                            .totalPrice(tTotal)
                            .build());
                }
            }

            // Tính tiền của nguyên 1 dòng sản phẩm = (Giá ly + Giá các topping) * Số lượng ly
            BigDecimal itemTotalPrice = (item.getPriceAtAdd().add(toppingsTotal))
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            // Cộng dồn vào tổng tiền giỏ hàng
            cartTotalPrice = cartTotalPrice.add(itemTotalPrice);

            // Bọc lại thành DTO
            itemResponses.add(CartItemResponse.builder()
                    .id(item.getId())
                    .variantId(item.getVariant().getId())
                    .productName(item.getVariant().getProduct().getName())
                    .productSlug(item.getVariant().getProduct().getSlug())
                    .sizeName(item.getVariant().getSize().getName())
                    .productImage(item.getVariant().getProduct().getDefaultImage())
                    .quantity(item.getQuantity())
                    .basePrice(item.getPriceAtAdd())
                    .toppings(toppingResponses)
                    .itemTotalPrice(itemTotalPrice)
                    .build());
        }

        // 4. Đóng gói trả về
        return CartResponse.builder()
                .cartId(cart.getId())
                .cartItems(itemResponses)
                .totalItems(totalItems)
                .cartTotalPrice(cartTotalPrice)
                .build();
    }

    @Override
    @Transactional
    public CartResponse updateQuantity(Long cartItemId, Integer quantity, String sessionId, String userEmail) {
        // 1. Lấy CartItem từ Database
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món hàng này trong giỏ!"));

        // 2. Chốt chặn bảo mật: Kiểm tra xem món này có đúng là của người đang request không
        verifyCartOwnership(cartItem.getCart(), sessionId, userEmail);

        // 3. Chốt chặn Tồn kho (Nghiệp vụ F&B: Chỉ check còn bán hay tạm ngưng)
        if (cartItem.getVariant().getInStock() == null || cartItem.getVariant().getInStock() == 0) {
            throw new RuntimeException("Sản phẩm này hiện đang tạm hết hàng, không thể tăng thêm số lượng!");
        }

        // 4. Cập nhật số lượng mới và Lưu
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        cartItemRepository.flush();

        // 5. [TỐI ƯU]: Gọi lại hàm getCart để trả về giỏ hàng mới nhất ngay lập tức
        return getCart(sessionId, userEmail);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long cartItemId, String sessionId, String userEmail) {
        // 1. Tìm món hàng
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món hàng này trong giỏ!"));

        Cart cart = cartItem.getCart();

        // 2. Bảo mật quyền sở hữu
        verifyCartOwnership(cart, sessionId, userEmail);

        cart.getCartItems().remove(cartItem);

        // 3. Ra lệnh xóa (JPA sẽ tự động xóa sạch CartItemTopping đi kèm nhờ orphanRemoval = true)
        cartItemRepository.delete(cartItem);

        // ÉP Hibernate đẩy ngay câu lệnh DELETE xuống MySQL lập tức
        cartItemRepository.flush();

        // 4. Trả về giỏ hàng mới sau khi xóa
        return getCart(sessionId, userEmail);
    }

    // ==========================================================
    // HÀM BỔ TRỢ: KIỂM TRA BẢO MẬT (OWNERSHIP)
    // ==========================================================
    private void verifyCartOwnership(Cart cart, String sessionId, String userEmail) {
        if (userEmail != null) {
            // Khách đã đăng nhập: Giỏ hàng phải thuộc về User này
            if (cart.getUser() == null || !userEmail.equals(cart.getUser().getEmail())) {
                throw new RuntimeException("Lỗi bảo mật: Bạn không có quyền thao tác trên giỏ hàng này!");
            }
        } else {
            // Khách vãng lai: Giỏ hàng phải trùng Session ID
            if (cart.getSessionId() == null || !cart.getSessionId().equals(sessionId)) {
                throw new RuntimeException("Lỗi bảo mật: Phiên làm việc không hợp lệ!");
            }
        }
    }
}
