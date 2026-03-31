package com.example.trasuaweb_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", length = 100, unique = true)
    private String orderNo;

    // Guest sẽ không có userId, User sẽ có
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "shipping_fee", precision = 10, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "pay_status", length = 50)
    private String payStatus; // UNPAID, PAID, FAILED

    @Column(name = "order_status", length = 50)
    private String orderStatus; // PENDING, PROCESSING, DELIVERING, COMPLETED, CANCELLED

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // COD, VNPAY, MOMO

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "internal_note", columnDefinition = "TEXT")
    private String internalNote; // Ghi chú của khách hàng

    @Column(name = "session_id", length = 255)
    private String sessionId;
    // ==========================================
    // QUAN HỆ VỚI ORDER ITEMS (CỰC KỲ QUAN TRỌNG)
    // ==========================================
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (orderStatus == null) orderStatus = "PENDING";
        if (payStatus == null) payStatus = "UNPAID";
        if (shippingFee == null) shippingFee = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}