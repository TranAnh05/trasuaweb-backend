package com.example.trasuaweb_backend.repositories;

import com.example.trasuaweb_backend.entities.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
}
