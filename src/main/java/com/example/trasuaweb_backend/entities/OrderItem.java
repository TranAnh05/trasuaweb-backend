package com.example.trasuaweb_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    // Snapshot: Chụp lại tên món và size để lỡ sau này admin đổi tên cũng không ảnh hưởng lịch sử
    @Column(name = "product_name_snapshot")
    private String productNameSnapshot;

    @Column(name = "size_name_snapshot", length = 100)
    private String sizeNameSnapshot;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice; // Giá 1 ly tại thời điểm mua

    private Integer quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal; // = unitPrice * quantity (Chưa tính topping)

    // Nếu em muốn lưu trữ tùy chọn đặc biệt dưới dạng JSON (ít đá, nhiều đường...)
    @Column(columnDefinition = "JSON")
    private String customizations;

    // ==========================================
    // QUAN HỆ VỚI ORDER ITEM TOPPINGS
    // ==========================================
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemTopping> toppings = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
