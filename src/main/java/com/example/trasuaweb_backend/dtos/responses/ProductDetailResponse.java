package com.example.trasuaweb_backend.dtos.responses;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String defaultImage;
    private List<String> images; // Danh sách các hình ảnh phụ

    // Thống kê đánh giá
    private Double averageRating;
    private Integer reviewCount;

    // Giá cơ bản (Là giá của Variant rẻ nhất)
    private BigDecimal basePrice;

    // Danh sách các Size (Biến thể)
    private List<VariantDto> variants;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VariantDto {
        private Long id;
        private String sku;
        private String sizeName; // "M", "L", "XL"
        private BigDecimal price;
        private Integer inStock;
    }
}
