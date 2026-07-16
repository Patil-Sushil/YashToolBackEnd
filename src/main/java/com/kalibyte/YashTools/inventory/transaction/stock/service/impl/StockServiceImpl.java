package com.kalibyte.YashTools.inventory.transaction.stock.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.master.materialgrade.repository.MaterialGradeRepository;
import com.kalibyte.YashTools.inventory.transaction.stock.dto.StockResponse;
import com.kalibyte.YashTools.inventory.transaction.stock.entity.Stock;
import com.kalibyte.YashTools.inventory.transaction.stock.mapper.StockMapper;
import com.kalibyte.YashTools.inventory.transaction.stock.repository.StockRepository;
import com.kalibyte.YashTools.inventory.transaction.stock.service.StockService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockMapper stockMapper;
    private final ItemRepository itemRepository;
    private final MaterialGradeRepository materialGradeRepository;

    public StockServiceImpl(StockRepository stockRepository, StockMapper stockMapper,
                            ItemRepository itemRepository,
                            MaterialGradeRepository materialGradeRepository) {
        this.stockRepository = stockRepository;
        this.stockMapper = stockMapper;
        this.itemRepository = itemRepository;
        this.materialGradeRepository = materialGradeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public StockResponse getStock(UUID itemId, UUID materialGradeId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + itemId));
        final MaterialGrade finalGrade;
        if (materialGradeId != null) {
            finalGrade = materialGradeRepository.findById(materialGradeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Material Grade not found with ID: " + materialGradeId));
        } else {
            finalGrade = null;
        }

        Stock stock = stockRepository.findStock(item, finalGrade)
                .orElseGet(() -> Stock.builder()
                        .item(item)
                        .materialGrade(finalGrade)
                        .quantity(BigDecimal.ZERO)
                        .build());

        return stockMapper.toResponse(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockResponse> getAllStocks(int page, int size) {
        Page<Stock> stockPage = stockRepository.findAll(PageRequest.of(page, size));
        return PageResponse.from(stockPage, stockMapper::toResponse);
    }

    @Override
    @Transactional
    public void addStock(Item item, MaterialGrade materialGrade, BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity to add must be greater than zero");
        }

        Stock stock = stockRepository.findStock(item, materialGrade)
                .orElseGet(() -> Stock.builder()
                        .item(item)
                        .materialGrade(materialGrade)
                        .quantity(BigDecimal.ZERO)
                        .build());

        stock.setQuantity(stock.getQuantity().add(quantity));
        stockRepository.save(stock);
    }

    @Override
    @Transactional
    public void deductStock(Item item, MaterialGrade materialGrade, BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity to deduct must be greater than zero");
        }

        String gradeStr = (materialGrade != null) ? materialGrade.getName() : "N/A";
        Stock stock = stockRepository.findStock(item, materialGrade)
                .orElseThrow(() -> new BusinessException("No raw material available in stock. Please purchase the raw material (Item: " 
                        + item.getName() + ", Grade: " + gradeStr + ")"));

        if (stock.getQuantity().compareTo(quantity) < 0) {
            throw new BusinessException("Insufficient stock. Available: " + stock.getQuantity() 
                    + ", Required: " + quantity + ". Please purchase the raw material (Item: " 
                    + item.getName() + ", Grade: " + gradeStr + ")");
        }

        stock.setQuantity(stock.getQuantity().subtract(quantity));
        stockRepository.save(stock);
    }
}
