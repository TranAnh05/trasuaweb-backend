package com.example.trasuaweb_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    // Quan hệ cha - con (Tự tham chiếu). Ví dụ: Trà trái cây (cha) -> Trà đào (con)
    @Column(name = "parent_id")
    private Long parentId;

    @Column(columnDefinition = "TEXT")
    private String description;

    // TRƯỜNG QUAN TRỌNG ĐỂ FIX LỖI:
    @Column(name = "sort_order")
    private Integer sortOrder;

    private Boolean active;

    // --- CỤM KHUYẾN MÃI (Flash Sale) ---
    @Column(name = "discount_type", length = 50)
    private String discountType;

    @Column(name = "discount_value", precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "discount_start_date")
    private LocalDateTime discountStartDate;

    @Column(name = "discount_end_date")
    private LocalDateTime discountEndDate;

    // --- THỜI GIAN TẠO & CẬP NHẬT ---
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Tự động set giá trị mặc định khi INSERT mới vào Database
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) active = true;
        if (sortOrder == null) sortOrder = 0;
    }

    // Tự động cập nhật thời gian khi UPDATE dòng dữ liệu
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}