package com.example.trasuaweb_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_item_toppings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemTopping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Khóa ngoại liên kết ngược lại với CartItem (Ly trà sữa nào đang chứa topping này)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_item_id", nullable = false)
    private CartItem cartItem;

    // Khóa ngoại liên kết với Topping (Đây là topping gì: Trân châu, Pudding...)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topping_id", nullable = false)
    private Topping topping;

    @Column(nullable = false)
    private Integer quantity;

    // Lưu lại giá của topping TẠI THỜI ĐIỂM THÊM VÀO GIỎ.
    // Rất quan trọng để lỡ ngày mai admin tăng giá topping thì giỏ hàng cũ của khách không bị đổi giá vô lý.
    @Column(name = "price_at_add", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtAdd;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Mặc định nếu không truyền số lượng thì coi như khách gọi 1 phần topping đó
        if (quantity == null) {
            quantity = 1;
        }
    }
}