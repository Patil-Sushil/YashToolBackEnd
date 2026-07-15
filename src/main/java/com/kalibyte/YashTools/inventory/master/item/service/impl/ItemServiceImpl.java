package com.kalibyte.YashTools.inventory.master.item.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.category.entity.Category;
import com.kalibyte.YashTools.inventory.master.category.repository.CategoryRepository;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.master.materialgrade.repository.MaterialGradeRepository;
import com.kalibyte.YashTools.inventory.master.item.dto.ItemRequest;
import com.kalibyte.YashTools.inventory.master.item.dto.ItemResponse;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.item.mapper.ItemMapper;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.inventory.master.item.service.ItemService;
import com.kalibyte.YashTools.inventory.master.item.specification.ItemSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final CategoryRepository categoryRepository;
    private final MaterialGradeRepository materialGradeRepository;

    public ItemServiceImpl(ItemRepository itemRepository, ItemMapper itemMapper,
                           CategoryRepository categoryRepository, MaterialGradeRepository materialGradeRepository) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.categoryRepository = categoryRepository;
        this.materialGradeRepository = materialGradeRepository;
    }

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        itemRepository.findBySku(request.getSku().trim())
                .ifPresent(i -> {
                    throw new BusinessException("Item SKU already exists: " + request.getSku());
                });

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        MaterialGrade materialGrade = null;
        if (request.getMaterialGradeId() != null) {
            materialGrade = materialGradeRepository.findById(request.getMaterialGradeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material Grade not found with ID: " + request.getMaterialGradeId()));
        }

        Item item = itemMapper.toEntity(request);
        item.setSku(item.getSku().trim());
        item.setName(item.getName().trim());
        item.setCategory(category);
        item.setMaterialGrade(materialGrade);

        return itemMapper.toResponse(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemResponse updateItem(UUID id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + id));

        itemRepository.findBySku(request.getSku().trim())
                .filter(i -> !i.getId().equals(id))
                .ifPresent(i -> {
                    throw new BusinessException("Item SKU already exists: " + request.getSku());
                });

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        MaterialGrade materialGrade = null;
        if (request.getMaterialGradeId() != null) {
            materialGrade = materialGradeRepository.findById(request.getMaterialGradeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material Grade not found with ID: " + request.getMaterialGradeId()));
        }

        itemMapper.updateEntityFromRequest(request, item);
        item.setSku(item.getSku().trim());
        item.setName(item.getName().trim());
        item.setCategory(category);
        item.setMaterialGrade(materialGrade);

        return itemMapper.toResponse(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemById(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + id));
        return itemMapper.toResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
                .map(itemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> getAllItems(int page, int size) {
        Page<Item> itemPage = itemRepository.findAll(PageRequest.of(page, size));
        return PageResponse.from(itemPage, itemMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> searchItems(UUID categoryId, UUID materialGradeId, Boolean active, String search, int page, int size) {
        Specification<Item> spec = Specification.where(ItemSpecification.hasCategory(categoryId))
                .and(ItemSpecification.hasMaterialGrade(materialGradeId))
                .and(ItemSpecification.isActive(active))
                .and(ItemSpecification.search(search));

        Page<Item> itemPage = itemRepository.findAll(spec, PageRequest.of(page, size));
        return PageResponse.from(itemPage, itemMapper::toResponse);
    }
}
