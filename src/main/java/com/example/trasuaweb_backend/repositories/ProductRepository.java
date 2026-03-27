package com.example.trasuaweb_backend.repositories;

import com.example.trasuaweb_backend.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Câu query JPQL thông minh: Lọc theo từ khóa và danh mục (nếu có)
    @Query("SELECT p FROM Product p WHERE p.status = 'published' " +
            "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categorySlug IS NULL OR p.category.slug = :categorySlug)")
    Page<Product> searchAndFilterProducts(
            @Param("keyword") String keyword,
            @Param("categorySlug") String categorySlug,
            Pageable pageable
    );
}