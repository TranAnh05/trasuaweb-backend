package com.example.trasuaweb_backend.repositories;

import com.example.trasuaweb_backend.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 1. Kiểm tra thêm :keyword = '' và :categorySlug = ''
    // 2. Thêm điều kiện p.category.parentId IN (...) để quét luôn các sản phẩm nằm trong danh mục con
    @Query("SELECT p FROM Product p WHERE p.status = 'published' " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categorySlug IS NULL OR :categorySlug = '' OR " +
            "     p.category.slug = :categorySlug OR " +
            "     p.category.parentId IN (SELECT c.id FROM Category c WHERE c.slug = :categorySlug))")
    Page<Product> searchAndFilterProducts(
            @Param("keyword") String keyword,
            @Param("categorySlug") String categorySlug,
            Pageable pageable
    );

    // Fetch luôn variants và images khi tìm kiếm theo slug để tối ưu hiệu suất
    @EntityGraph(attributePaths = {"variants", "variants.size", "images"})
    Optional<Product> findBySlugAndStatus(String slug, String status);
}