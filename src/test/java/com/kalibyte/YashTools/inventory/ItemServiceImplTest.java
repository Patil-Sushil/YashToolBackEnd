package com.kalibyte.YashTools.inventory;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.inventory.master.category.entity.Category;
import com.kalibyte.YashTools.inventory.master.category.repository.CategoryRepository;
import com.kalibyte.YashTools.inventory.master.item.dto.ItemRequest;
import com.kalibyte.YashTools.inventory.master.item.dto.ItemResponse;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.item.mapper.ItemMapper;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.inventory.master.item.service.impl.ItemServiceImpl;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.master.materialgrade.repository.MaterialGradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MaterialGradeRepository materialGradeRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createItem_Success() {
        UUID categoryId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        ItemRequest request = ItemRequest.builder()
                .name("Carbide Rod")
                .sku("CR-123")
                .categoryId(categoryId)
                .materialGradeId(gradeId)
                .active(true)
                .build();

        Category category = Category.builder().name("Raw Material").code("RM").build();
        MaterialGrade grade = MaterialGrade.builder().name("K40UF").code("K40").build();
        Item item = Item.builder().name("Carbide Rod").sku("CR-123").build();
        ItemResponse response = ItemResponse.builder().name("Carbide Rod").sku("CR-123").build();

        when(itemRepository.findBySku("CR-123")).thenReturn(Optional.empty());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(materialGradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
        when(itemMapper.toEntity(request)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toResponse(item)).thenReturn(response);

        ItemResponse result = itemService.createItem(request);

        assertNotNull(result);
        assertEquals("Carbide Rod", result.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void createItem_DuplicateSku_ThrowsException() {
        ItemRequest request = ItemRequest.builder()
                .name("Carbide Rod")
                .sku("CR-123")
                .build();

        when(itemRepository.findBySku("CR-123")).thenReturn(Optional.of(new Item()));

        assertThrows(BusinessException.class, () -> itemService.createItem(request));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void getItemById_NotFound_ThrowsException() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.getItemById(id));
    }
}
