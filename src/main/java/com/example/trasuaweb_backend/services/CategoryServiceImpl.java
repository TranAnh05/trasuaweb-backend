package com.example.trasuaweb_backend.services;

import com.example.trasuaweb_backend.dtos.responses.CategoryResponse;
import com.example.trasuaweb_backend.entities.Category;
import com.example.trasuaweb_backend.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getActiveCategories() {
        // 1. Lấy TẤT CẢ danh mục active lên (Chỉ tốn 1 câu query duy nhất)
        List<Category> allCategories = categoryRepository.findAllByActiveTrueOrderBySortOrderAsc();

        // 2. Chuyển toàn bộ Entity sang DTO
        List<CategoryResponse> allDtos = allCategories.stream()
                .map(category -> CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .slug(category.getSlug())
                        .parentId(category.getParentId()) // Map thêm parentId
                        .build())
                .collect(Collectors.toList());

        // 3. Gom nhóm các category con theo ID của thằng cha
        // Kết quả: Map<ID_Cha, Danh_sách_các_thằng_con>
        Map<Long, List<CategoryResponse>> childrenMap = allDtos.stream()
                .filter(dto -> dto.getParentId() != null)
                .collect(Collectors.groupingBy(CategoryResponse::getParentId));

        // 4. Lắp ráp con vào cha
        allDtos.forEach(dto -> {
            List<CategoryResponse> children = childrenMap.get(dto.getId());
            if (children != null) {
                dto.setChildren(children);
            }
        });

        // 5. Cuối cùng, chỉ trả về các Danh mục ROOT (Cha cao nhất, có parentId == null)
        // Khi trả về ROOT, nó sẽ tự động kéo theo toàn bộ children bên trong nó do ta đã lắp ráp ở bước 4
        return allDtos.stream()
                .filter(dto -> dto.getParentId() == null)
                .collect(Collectors.toList());
    }
}
