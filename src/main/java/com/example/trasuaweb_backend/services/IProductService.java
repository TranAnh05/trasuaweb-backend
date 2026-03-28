package com.example.trasuaweb_backend.services;

import com.example.trasuaweb_backend.dtos.responses.ProductDetailResponse;
import com.example.trasuaweb_backend.dtos.responses.ProductResponse;
import org.springframework.data.domain.Page;

public interface IProductService {
    Page<ProductResponse> getProducts(String keyword, String categorySlug, int pageNo, int pageSize, String sortBy);
    ProductDetailResponse getProductDetailBySlug(String slug);
}
