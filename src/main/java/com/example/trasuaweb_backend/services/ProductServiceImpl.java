package com.example.trasuaweb_backend.services;


import com.example.trasuaweb_backend.dtos.responses.ProductResponse;
import com.example.trasuaweb_backend.entities.Product;
import com.example.trasuaweb_backend.entities.ProductVariant;
import com.example.trasuaweb_backend.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor // tự động tạo Constructor để inject ProductRepository
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;

    @Override
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
        List<ProductVariant> variants = product.getVariants();

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
}
