package com.kalibyte.YashTools.master.ratechart.service.impl;

import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.master.ratechart.dto.request.HyperionCoolantHoleRodPriceRequest;
import com.kalibyte.YashTools.master.ratechart.dto.response.HyperionCoolantHoleRodPriceResponse;
import com.kalibyte.YashTools.master.ratechart.entity.HyperionCoolantHoleRodPrice;
import com.kalibyte.YashTools.master.ratechart.mapper.HyperionCoolantHoleRodPriceMapper;
import com.kalibyte.YashTools.master.ratechart.repository.HyperionCoolantHoleRodPriceRepository;
import com.kalibyte.YashTools.master.ratechart.service.HyperionCoolantHoleRodPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HyperionCoolantHoleRodPriceServiceImpl implements HyperionCoolantHoleRodPriceService {

    private final HyperionCoolantHoleRodPriceRepository repository;
    private final HyperionCoolantHoleRodPriceMapper mapper;

    @Override
    @LoggableAction("CREATE_COOLANT_HOLE_PRICE")
    public HyperionCoolantHoleRodPriceResponse create(HyperionCoolantHoleRodPriceRequest request) {
        log.debug("Creating coolant hole price for category: {}, item: {}", request.getCategory(), request.getItem());

        if (repository.existsByCategoryIgnoreCaseAndItemIgnoreCase(request.getCategory(), request.getItem())) {
            log.warn("Duplicate entry attempt: category={}, item={}", request.getCategory(), request.getItem());
            throw new BusinessException("Item and category combination already exists in Coolant Hole Rod Price rate chart");
        }

        HyperionCoolantHoleRodPrice entity = mapper.toEntity(request);
        HyperionCoolantHoleRodPrice saved = repository.save(entity);
        log.info("Successfully created coolant hole price with id: {}", saved.getId());

        return mapper.toResponse(saved);
    }

    @Override
    @LoggableAction("UPDATE_COOLANT_HOLE_PRICE")
    public HyperionCoolantHoleRodPriceResponse update(UUID id, HyperionCoolantHoleRodPriceRequest request) {
        log.debug("Updating coolant hole price with id: {}", id);

        HyperionCoolantHoleRodPrice entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate chart entry not found with id: " + id));

        boolean isUniqueFieldChanged = !entity.getCategory().equalsIgnoreCase(request.getCategory())
                || !entity.getItem().equalsIgnoreCase(request.getItem());

        if (isUniqueFieldChanged && repository.existsByCategoryIgnoreCaseAndItemIgnoreCase(
                request.getCategory(), request.getItem())) {
            log.warn("Duplicate entry during update: category={}, item={}", request.getCategory(), request.getItem());
            throw new BusinessException("Another entry with this category and item name combination already exists");
        }

        entity.setCategory(request.getCategory());
        entity.setItem(request.getItem());
        entity.setPrice(request.getPrice());

        HyperionCoolantHoleRodPrice updated = repository.save(entity);
        log.info("Successfully updated coolant hole price with id: {}", id);

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HyperionCoolantHoleRodPriceResponse> getAllActive() {
        log.debug("Fetching all active coolant hole prices");
        List<HyperionCoolantHoleRodPrice> entities = repository.findByActiveTrue();
        log.info("Found {} active coolant hole prices", entities.size());
        return mapper.toResponseList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HyperionCoolantHoleRodPriceResponse> getAllActivePaginated(Pageable pageable) {
        log.debug("Fetching paginated active coolant hole prices: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<HyperionCoolantHoleRodPrice> page = repository.findByActiveTrue(pageable);
        log.info("Found {} active coolant hole prices in page {} of {}",
                page.getNumberOfElements(), page.getNumber(), page.getTotalPages());
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public HyperionCoolantHoleRodPriceResponse getById(UUID id) {
        log.debug("Fetching coolant hole price by id: {}", id);
        HyperionCoolantHoleRodPrice entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate chart entry not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    @LoggableAction("DEACTIVATE_COOLANT_HOLE_PRICE")
    public void deactivate(UUID id) {
        log.debug("Deactivating coolant hole price with id: {}", id);
        HyperionCoolantHoleRodPrice entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate chart entry not found with id: " + id));
        entity.setActive(false);
        repository.save(entity);
        log.info("Successfully deactivated coolant hole price with id: {}", id);
    }

    @Override
    @LoggableAction("IMPORT_COOLANT_HOLE_PRICE")
    public int importExcel(InputStream inputStream) {
        log.info("Starting Excel import for coolant hole prices");

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new BusinessException("The Excel sheet is empty");
            }

            // Find column indices
            int categoryCol = findColumnIndex(headerRow, "category");
            int itemCol = findColumnIndex(headerRow, "item");
            int priceCol = findColumnIndex(headerRow, "price");

            if (categoryCol == -1 || itemCol == -1 || priceCol == -1) {
                throw new BusinessException("Required columns not found in Excel sheet. " +
                        "Please ensure columns: Category, Item, Price are present.");
            }

            int count = 0;
            int lastRowNum = sheet.getLastRowNum();

            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String category = getCellValueAsString(row.getCell(categoryCol)).trim();
                String item = getCellValueAsString(row.getCell(itemCol)).trim();

                // Skip completely empty rows
                if (category.isEmpty() && item.isEmpty()) {
                    continue;
                }

                // Validate required fields
                if (category.isEmpty() || item.isEmpty()) {
                    throw new BusinessException("Category and Item columns cannot be empty at row " + (r + 1));
                }

                BigDecimal price = BigDecimal.valueOf(getCellValueAsDouble(row.getCell(priceCol)));

                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("Price must be greater than 0 at row " + (r + 1));
                }

                // Update or create
                Optional<HyperionCoolantHoleRodPrice> existingOpt =
                        repository.findByCategoryIgnoreCaseAndItemIgnoreCase(category, item);

                HyperionCoolantHoleRodPrice entity;
                if (existingOpt.isPresent()) {
                    entity = existingOpt.get();
                    entity.setPrice(price);
                    entity.setActive(true);
                    log.debug("Updating existing entry: category={}, item={}", category, item);
                } else {
                    entity = HyperionCoolantHoleRodPrice.builder()
                            .category(category)
                            .item(item)
                            .price(price)
                            .active(true)
                            .build();
                    log.debug("Creating new entry: category={}, item={}", category, item);
                }

                repository.save(entity);
                count++;
            }

            log.info("Successfully imported {} coolant hole rod prices", count);
            return count;

        } catch (Exception e) {
            log.error("Failed to import Excel file", e);
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException("Failed to import Excel file: " + e.getMessage());
        }
    }

    private int findColumnIndex(Row headerRow, String columnName) {
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && getCellValueAsString(cell).trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                double numericValue = cell.getNumericCellValue();
                // Remove decimal point for whole numbers
                if (numericValue == (long) numericValue) {
                    yield String.valueOf((long) numericValue);
                }
                yield String.valueOf(numericValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        yield String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        yield "";
                    }
                }
            }
            default -> "";
        };
    }

    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) {
            return 0.0;
        }

        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    yield 0.0;
                }
            }
            case FORMULA -> {
                try {
                    yield cell.getNumericCellValue();
                } catch (Exception e) {
                    try {
                        yield Double.parseDouble(cell.getStringCellValue().trim());
                    } catch (Exception ex) {
                        yield 0.0;
                    }
                }
            }
            default -> 0.0;
        };
    }
}