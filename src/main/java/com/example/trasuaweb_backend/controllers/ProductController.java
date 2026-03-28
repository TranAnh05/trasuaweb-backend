package com.example.trasuaweb_backend.controllers;

import com.example.trasuaweb_backend.dtos.responses.ApiResponse;
import com.example.trasuaweb_backend.dtos.responses.ProductDetailResponse;
import com.example.trasuaweb_backend.dtos.responses.ProductResponse;
import com.example.trasuaweb_backend.services.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String categorySlug,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "12") int limit,
            @RequestParam(value = "sort", defaultValue = "newest") String sort
    ) {
        // 1. Gọi Service xử lý logic
        Page<ProductResponse> productPage = productService.getProducts(keyword, categorySlug, page, limit, sort);

        // 2. Đóng gói dữ liệu vào ApiResponse
        ApiResponse<Page<ProductResponse>> response = ApiResponse.<Page<ProductResponse>>builder()
                .status(200)
                .message("Lấy danh sách sản phẩm thành công")
                .data(productPage)
                .build();

        // 3. Trả về HTTP Status 200 OK
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable String slug) {
        ProductDetailResponse productDetail = productService.getProductDetailBySlug(slug);

        return ResponseEntity.ok(ApiResponse.<ProductDetailResponse>builder()
                .status(200)
                .message("Lấy chi tiết sản phẩm thành công")
                .data(productDetail)
                .build());
    }
}