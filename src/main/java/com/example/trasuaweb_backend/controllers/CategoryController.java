package com.example.trasuaweb_backend.controllers;

import com.example.trasuaweb_backend.dtos.responses.ApiResponse;
import com.example.trasuaweb_backend.dtos.responses.CategoryResponse;
import com.example.trasuaweb_backend.services.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final ICategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> categories = categoryService.getActiveCategories();

        ApiResponse<List<CategoryResponse>> response = ApiResponse.<List<CategoryResponse>>builder()
                .status(200)
                .message("Lấy danh sách danh mục thành công")
                .data(categories)
                .build();

        return ResponseEntity.ok(response);
    }
}
