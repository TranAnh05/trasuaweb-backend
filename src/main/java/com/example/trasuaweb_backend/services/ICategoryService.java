package com.example.trasuaweb_backend.services;

import com.example.trasuaweb_backend.dtos.responses.CategoryResponse;

import java.util.List;

public interface ICategoryService {
    List<CategoryResponse> getActiveCategories();
}
