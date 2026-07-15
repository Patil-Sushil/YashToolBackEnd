package com.kalibyte.YashTools.inventory.master.category.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.category.dto.CategoryRequest;
import com.kalibyte.YashTools.inventory.master.category.dto.CategoryResponse;
import com.kalibyte.YashTools.inventory.master.category.entity.Category;
import com.kalibyte.YashTools.inventory.master.category.mapper.CategoryMapper;
import com.kalibyte.YashTools.inventory.master.category.repository.CategoryRepository;
import com.kalibyte.YashTools.inventory.master.category.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        categoryRepository.findByCode(request.getCode().trim())
                .ifPresent(c -> {
                    throw new BusinessException("Category code already exists: " + request.getCode());
                });

        categoryRepository.findByName(request.getName().trim())
                .ifPresent(c -> {
                    throw new BusinessException("Category name already exists: " + request.getName());
                });

        Category category = categoryMapper.toEntity(request);
        category.setCode(category.getCode().trim());
        category.setName(category.getName().trim());

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        categoryRepository.findByCode(request.getCode().trim())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new BusinessException("Category code already exists: " + request.getCode());
                });

        categoryRepository.findByName(request.getName().trim())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new BusinessException("Category name already exists: " + request.getName());
                });

        categoryMapper.updateEntityFromRequest(request, category);
        category.setCode(category.getCode().trim());
        category.setName(category.getName().trim());

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getAllCategories(int page, int size) {
        Page<Category> categoryPage = categoryRepository.findAll(PageRequest.of(page, size));
        return PageResponse.from(categoryPage, categoryMapper::toResponse);
    }
}
