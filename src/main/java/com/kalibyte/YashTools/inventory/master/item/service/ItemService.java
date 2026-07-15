package com.kalibyte.YashTools.inventory.master.item.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.item.dto.ItemRequest;
import com.kalibyte.YashTools.inventory.master.item.dto.ItemResponse;

import java.util.List;
import java.util.UUID;

public interface ItemService {
    ItemResponse createItem(ItemRequest request);
    ItemResponse updateItem(UUID id, ItemRequest request);
    ItemResponse getItemById(UUID id);
    List<ItemResponse> getAllItems();
    PageResponse<ItemResponse> getAllItems(int page, int size);
    PageResponse<ItemResponse> searchItems(UUID categoryId, UUID materialGradeId, Boolean active, String search, int page, int size);
}
