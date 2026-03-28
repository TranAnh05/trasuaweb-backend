package com.example.trasuaweb_backend.services;


import com.example.trasuaweb_backend.dtos.responses.ProductDetailResponse;
import com.example.trasuaweb_backend.dtos.responses.ProductResponse;
import com.example.trasuaweb_backend.entities.Product;
import com.example.trasuaweb_backend.entities.ProductImage;
import com.example.trasuaweb_backend.entities.ProductVariant;
import com.example.trasuaweb_backend.repositories.ProductRepository;
import com.example.trasuaweb_backend.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // tự động tạo Constructor để inject ProductRepository
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional(readOnly = true) // tránh lỗi LazyInitializationException khi gọi .getVariants()
    public Page<ProductResponse> getProducts(String keyword, String categorySlug, int pageNo, int pageSize, String sortBy) {

        // 1. Xử lý logic sắp xếp (Sort)
        Sort sort = Sort.by("createdAt").descending(); // Mặc định mới nhất
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            // Lưu ý: Sắp xếp theo giá cần join bảng variant, ở mức cơ bản ta sort theo viewCount hoặc ngày tạo trước
            sort = Sort.by("viewCount").descending();
        }

        // 2. Tạo đối tượng Pageable (Spring Boot đếm trang từ 0)
        Pageable pageable = PageRequest.of(pageNo > 0 ? pageNo - 1 : 0, pageSize, sort);

        // 3. Lấy dữ liệu từ DB (Lúc này trả về Page chứa Entity)
        Page<Product> productPage = productRepository.searchAndFilterProducts(keyword, categorySlug, pageable);

        // 4. Map Entity sang DTO và tính toán Min/Max Price
        return productPage.map(this::mapToProductResponse);
    }

    // Hàm private để chuyển đổi Product Entity -> ProductResponse DTO
    private ProductResponse mapToProductResponse(Product product) {
        Set<ProductVariant> variants = product.getVariants();

        BigDecimal minPrice = BigDecimal.ZERO;
        BigDecimal maxPrice = BigDecimal.ZERO;

        if (variants != null && !variants.isEmpty()) {
            // Tìm giá nhỏ nhất và lớn nhất trong danh sách size
            minPrice = variants.stream().map(ProductVariant::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            maxPrice = variants.stream().map(ProductVariant::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        }

        // Giả sử sản phẩm tạo trong vòng 7 ngày thì gắn mác "NEW"
        boolean isNew = product.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7));

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .defaultImage(product.getDefaultImage())
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .isNew(isNew)
                .build();
    }

    @Override
    @Transactional(readOnly = true) // Báo cho Spring biết đây là hàm chỉ đọc dữ liệu
    public ProductDetailResponse getProductDetailBySlug(String slug) {
        // 1. Tìm sản phẩm (chỉ lấy SP đang published)
        Product product = productRepository.findBySlugAndStatus(slug, "published")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm")); // Sau này có thể thay bằng Custom Exception (ResourceNotFoundException)

        // 2. Chuyển đổi danh sách Variants sang DTO
        List<ProductDetailResponse.VariantDto> variantDtos = product.getVariants().stream()
                .map(v -> ProductDetailResponse.VariantDto.builder()
                        .id(v.getId())
                        .sku(v.getSku())
                        .sizeName(v.getSize().getName())
                        .price(v.getPrice())
                        .inStock(v.getInStock())
                        .build())
                .collect(Collectors.toList());

        // 3. Tìm giá cơ bản (Giá nhỏ nhất trong các biến thể)
        BigDecimal basePrice = variantDtos.stream()
                .map(ProductDetailResponse.VariantDto::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 4. Lấy danh sách URL hình ảnh
        List<String> imageUrls = product.getImages().stream()
                .map(ProductImage::getUrl)
                .collect(Collectors.toList());

        // 5. Thống kê Review
        Double avgRating = reviewRepository.getAverageRatingByProductId(product.getId());
        Integer reviewCount = reviewRepository.countApprovedReviewsByProductId(product.getId());

        // 6. Build Response
        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .defaultImage(product.getDefaultImage())
                .images(imageUrls)
                .basePrice(basePrice)
                // Nếu chưa có ai rate thì trả về 5.0 mặc định cho đẹp UI
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 5.0)
                .reviewCount(reviewCount != null ? reviewCount : 0)
                .variants(variantDtos)
                .build();
    }
}
