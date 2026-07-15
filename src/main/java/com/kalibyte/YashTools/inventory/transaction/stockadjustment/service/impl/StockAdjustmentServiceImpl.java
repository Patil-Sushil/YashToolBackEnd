package com.kalibyte.YashTools.inventory.transaction.stockadjustment.service.impl;

import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.master.materialgrade.repository.MaterialGradeRepository;
import com.kalibyte.YashTools.inventory.shared.enums.StockAdjustmentType;
import com.kalibyte.YashTools.inventory.shared.enums.StockTransactionType;
import com.kalibyte.YashTools.inventory.transaction.stock.service.StockService;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.dto.StockAdjustmentRequest;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.dto.StockAdjustmentResponse;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.entity.StockAdjustment;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.mapper.StockAdjustmentMapper;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.repository.StockAdjustmentRepository;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.service.StockAdjustmentService;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.service.StockTransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class StockAdjustmentServiceImpl implements StockAdjustmentService {

    private final StockAdjustmentRepository repository;
    private final StockAdjustmentMapper mapper;
    private final ItemRepository itemRepository;
    private final MaterialGradeRepository materialGradeRepository;
    private final StockService stockService;
    private final StockTransactionService stockTransactionService;

    public StockAdjustmentServiceImpl(StockAdjustmentRepository repository, StockAdjustmentMapper mapper,
                                       ItemRepository itemRepository, MaterialGradeRepository materialGradeRepository,
                                       StockService stockService,
                                       StockTransactionService stockTransactionService) {
        this.repository = repository;
        this.mapper = mapper;
        this.itemRepository = itemRepository;
        this.materialGradeRepository = materialGradeRepository;
        this.stockService = stockService;
        this.stockTransactionService = stockTransactionService;
    }

    @Override
    @Transactional
    public StockAdjustmentResponse adjustStock(StockAdjustmentRequest request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + request.getItemId()));

        MaterialGrade materialGrade = null;
        if (request.getMaterialGradeId() != null) {
            materialGrade = materialGradeRepository.findById(request.getMaterialGradeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material Grade not found with ID: " + request.getMaterialGradeId()));
        }

        long count = repository.count();
        String adjustmentNumber = String.format("ADJ-%06d", count + 1);

        // Perform stock movement
        if (request.getAdjustmentType() == StockAdjustmentType.ADD) {
            stockService.addStock(item, materialGrade, request.getQuantity());
        } else {
            stockService.deductStock(item, materialGrade, request.getQuantity());
        }

        // Record stock transaction history
        stockTransactionService.createTransaction(
                StockTransactionType.STOCK_ADJUSTMENT,
                item,
                materialGrade,
                request.getQuantity(),
                adjustmentNumber,
                request.getRemarks() != null ? request.getRemarks() : "Manual Adjustment: " + request.getReason()
        );

        StockAdjustment adjustment = StockAdjustment.builder()
                .adjustmentNumber(adjustmentNumber)
                .item(item)
                .materialGrade(materialGrade)
                .quantity(request.getQuantity())
                .adjustmentType(request.getAdjustmentType())
                .reason(request.getReason())
                .remarks(request.getRemarks())
                .build();

        return mapper.toResponse(repository.save(adjustment));
    }

    @Override
    @Transactional(readOnly = true)
    public StockAdjustmentResponse getAdjustmentById(UUID id) {
        StockAdjustment adjustment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock adjustment not found with ID: " + id));
        return mapper.toResponse(adjustment);
    }

    @Override
    @Transactional(readOnly = true)
    public StockAdjustmentResponse getAdjustmentByNumber(String number) {
        StockAdjustment adjustment = repository.findByAdjustmentNumber(number.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Stock adjustment not found with number: " + number));
        return mapper.toResponse(adjustment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockAdjustmentResponse> getAllAdjustments(int page, int size) {
        Page<StockAdjustment> adjPage = repository.findAll(PageRequest.of(page, size));
        return PageResponse.from(adjPage, mapper::toResponse);
    }
}
