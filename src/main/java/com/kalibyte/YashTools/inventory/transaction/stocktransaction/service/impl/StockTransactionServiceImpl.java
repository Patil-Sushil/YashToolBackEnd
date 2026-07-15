package com.kalibyte.YashTools.inventory.transaction.stocktransaction.service.impl;

import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.shared.enums.StockTransactionType;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.dto.StockTransactionResponse;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.entity.StockTransaction;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.mapper.StockTransactionMapper;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.repository.StockTransactionRepository;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.service.StockTransactionService;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.specification.StockTransactionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class StockTransactionServiceImpl implements StockTransactionService {

    private final StockTransactionRepository repository;
    private final StockTransactionMapper mapper;

    public StockTransactionServiceImpl(StockTransactionRepository repository, StockTransactionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public StockTransactionResponse getTransactionById(UUID id) {
        StockTransaction tx = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock transaction not found with ID: " + id));
        return mapper.toResponse(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockTransactionResponse> searchTransactions(
            StockTransactionType type, UUID itemId, String referenceNumber, int page, int size) {
        
        Specification<StockTransaction> spec = Specification.where(StockTransactionSpecification.hasTransactionType(type))
                .and(StockTransactionSpecification.hasItem(itemId))
                .and(StockTransactionSpecification.hasReferenceNumber(referenceNumber));

        Page<StockTransaction> txPage = repository.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.from(txPage, mapper::toResponse);
    }

    @Override
    @Transactional
    public void createTransaction(
            StockTransactionType type,
            Item item,
            MaterialGrade materialGrade,
            BigDecimal quantity,
            String referenceNumber,
            String remarks) {

        StockTransaction tx = StockTransaction.builder()
                .transactionType(type)
                .item(item)
                .materialGrade(materialGrade)
                .quantity(quantity)
                .referenceNumber(referenceNumber)
                .remarks(remarks)
                .build();

        repository.save(tx);
    }
}
