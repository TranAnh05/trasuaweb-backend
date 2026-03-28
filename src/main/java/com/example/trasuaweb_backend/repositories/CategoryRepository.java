package com.example.trasuaweb_backend.repositories;

import com.example.trasuaweb_backend.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Chỉ lấy các danh mục đang được active (kích hoạt) và sắp xếp theo sortOrder
    List<Category> findAllByActiveTrueOrderBySortOrderAsc();
}
