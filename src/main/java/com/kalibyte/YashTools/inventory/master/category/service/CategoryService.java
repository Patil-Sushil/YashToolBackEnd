package com.kalibyte.YashTools.inventory.master.category.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.category.dto.CategoryRequest;
import com.kalibyte.YashTools.inventory.master.category.dto.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(UUID id, CategoryRequest request);
    CategoryResponse getCategoryById(UUID id);
    List<CategoryResponse> getAllCategories();
    PageResponse<CategoryResponse> getAllCategories(int page, int size);
}
