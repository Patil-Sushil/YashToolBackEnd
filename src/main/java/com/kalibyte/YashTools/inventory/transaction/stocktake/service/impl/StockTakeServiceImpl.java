package com.kalibyte.YashTools.inventory.transaction.stocktake.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.master.materialgrade.repository.MaterialGradeRepository;
import com.kalibyte.YashTools.inventory.shared.enums.StockAdjustmentReason;
import com.kalibyte.YashTools.inventory.shared.enums.StockAdjustmentType;
import com.kalibyte.YashTools.inventory.shared.enums.StockTakeStatus;
import com.kalibyte.YashTools.inventory.transaction.stock.dto.StockResponse;
import com.kalibyte.YashTools.inventory.transaction.stock.service.StockService;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.dto.StockAdjustmentRequest;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.service.StockAdjustmentService;
import com.kalibyte.YashTools.inventory.transaction.stocktake.dto.StockTakeLineRequest;
import com.kalibyte.YashTools.inventory.transaction.stocktake.dto.StockTakeRequest;
import com.kalibyte.YashTools.inventory.transaction.stocktake.dto.StockTakeResponse;
import com.kalibyte.YashTools.inventory.transaction.stocktake.entity.StockTake;
import com.kalibyte.YashTools.inventory.transaction.stocktake.entity.StockTakeLine;
import com.kalibyte.YashTools.inventory.transaction.stocktake.mapper.StockTakeMapper;
import com.kalibyte.YashTools.inventory.transaction.stocktake.repository.StockTakeRepository;
import com.kalibyte.YashTools.inventory.transaction.stocktake.service.StockTakeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class StockTakeServiceImpl implements StockTakeService {

    private final StockTakeRepository repository;
    private final StockTakeMapper mapper;
    private final ItemRepository itemRepository;
    private final MaterialGradeRepository materialGradeRepository;
    
    private final StockService stockService;
    private final StockAdjustmentService stockAdjustmentService;

    public StockTakeServiceImpl(StockTakeRepository repository, StockTakeMapper mapper,
                                ItemRepository itemRepository,
                                MaterialGradeRepository materialGradeRepository, StockService stockService,
                                StockAdjustmentService stockAdjustmentService) {
        this.repository = repository;
        this.mapper = mapper;
        this.itemRepository = itemRepository;
        this.materialGradeRepository = materialGradeRepository;
        this.stockService = stockService;
        this.stockAdjustmentService = stockAdjustmentService;
    }

    @Override
    @Transactional
    public StockTakeResponse createStockTake(StockTakeRequest request) {
        long count = repository.count();
        String stockTakeNumber = String.format("STK-%06d", count + 1);

        StockTake stockTake = StockTake.builder()
                .stockTakeNumber(stockTakeNumber)
                .status(StockTakeStatus.DRAFT)
                .remarks(request.getRemarks())
                .lines(new ArrayList<>())
                .build();

        for (StockTakeLineRequest lineReq : request.getLines()) {
            Item item = itemRepository.findById(lineReq.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + lineReq.getItemId()));

            MaterialGrade grade = null;
            if (lineReq.getMaterialGradeId() != null) {
                grade = materialGradeRepository.findById(lineReq.getMaterialGradeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Material Grade not found with ID: " + lineReq.getMaterialGradeId()));
            }

            // Retrieve current system quantity
            UUID gradeId = grade != null ? grade.getId() : null;
            StockResponse stockRes = stockService.getStock(item.getId(), gradeId);
            BigDecimal systemQty = stockRes.getQuantity();

            BigDecimal difference = lineReq.getPhysicalQuantity().subtract(systemQty);

            StockTakeLine line = StockTakeLine.builder()
                    .stockTake(stockTake)
                    .item(item)
                    .materialGrade(grade)
                    .systemQuantity(systemQty)
                    .physicalQuantity(lineReq.getPhysicalQuantity())
                    .differenceQuantity(difference)
                    .approved(false)
                    .build();

            stockTake.getLines().add(line);
        }

        return mapper.toResponse(repository.save(stockTake));
    }

    @Override
    @Transactional(readOnly = true)
    public StockTakeResponse getStockTakeById(UUID id) {
        StockTake stockTake = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock take not found with ID: " + id));
        return mapper.toResponse(stockTake);
    }

    @Override
    @Transactional(readOnly = true)
    public StockTakeResponse getStockTakeByNumber(String number) {
        StockTake stockTake = repository.findByStockTakeNumber(number.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Stock take not found with number: " + number));
        return mapper.toResponse(stockTake);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockTakeResponse> getAllStockTakes(int page, int size) {
        Page<StockTake> stockTakePage = repository.findAll(PageRequest.of(page, size));
        return PageResponse.from(stockTakePage, mapper::toResponse);
    }

    @Override
    @Transactional
    public StockTakeResponse completeStockTake(UUID id) {
        StockTake stockTake = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock take not found with ID: " + id));

        if (stockTake.getStatus() != StockTakeStatus.DRAFT) {
            throw new BusinessException("Only DRAFT stock takes can be completed");
        }

        stockTake.setStatus(StockTakeStatus.COMPLETED);
        return mapper.toResponse(repository.save(stockTake));
    }

    @Override
    @Transactional
    public StockTakeResponse approveStockTake(UUID id) {
        StockTake stockTake = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock take not found with ID: " + id));

        if (stockTake.getStatus() != StockTakeStatus.COMPLETED) {
            throw new BusinessException("Only COMPLETED stock takes can be approved");
        }

        stockTake.setStatus(StockTakeStatus.APPROVED);

        for (StockTakeLine line : stockTake.getLines()) {
            line.setApproved(true);

            // Reconcile difference if any
            if (line.getDifferenceQuantity().compareTo(BigDecimal.ZERO) != 0) {
                StockAdjustmentType adjType = line.getDifferenceQuantity().compareTo(BigDecimal.ZERO) > 0 
                        ? StockAdjustmentType.ADD 
                        : StockAdjustmentType.DEDUCT;

                StockAdjustmentRequest adjRequest = StockAdjustmentRequest.builder()
                        .itemId(line.getItem().getId())
                        .materialGradeId(line.getMaterialGrade() != null ? line.getMaterialGrade().getId() : null)
                        .quantity(line.getDifferenceQuantity().abs())
                        .adjustmentType(adjType)
                        .reason(StockAdjustmentReason.AUDIT_DIFFERENCE)
                        .remarks("Automatically generated from Stock Take approval: " + stockTake.getStockTakeNumber())
                        .build();

                stockAdjustmentService.adjustStock(adjRequest);
            }
        }

        return mapper.toResponse(repository.save(stockTake));
    }
}
