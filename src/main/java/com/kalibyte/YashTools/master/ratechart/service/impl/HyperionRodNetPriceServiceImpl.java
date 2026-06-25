package com.kalibyte.YashTools.master.ratechart.service.impl;

import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.master.ratechart.dto.request.HyperionRodNetPriceRequest;
import com.kalibyte.YashTools.master.ratechart.dto.response.HyperionRodNetPriceResponse;
import com.kalibyte.YashTools.master.ratechart.entity.HyperionRodNetPrice;
import com.kalibyte.YashTools.master.ratechart.mapper.HyperionRodNetPriceMapper;
import com.kalibyte.YashTools.master.ratechart.repository.HyperionRodNetPriceRepository;
import com.kalibyte.YashTools.master.ratechart.service.HyperionRodNetPriceService;
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
public class HyperionRodNetPriceServiceImpl implements HyperionRodNetPriceService {

    private final HyperionRodNetPriceRepository repository;
    private final HyperionRodNetPriceMapper mapper;

    @Override
    @LoggableAction("CREATE_ROD_NET_PRICE")
    public HyperionRodNetPriceResponse create(HyperionRodNetPriceRequest request) {
        log.debug("Creating rod net price for item: {}", request.getItem());

        if (repository.existsByItemIgnoreCase(request.getItem())) {
            log.warn("Duplicate entry attempt: item={}", request.getItem());
            throw new BusinessException("Item already exists in Rod Net Price rate chart");
        }

        HyperionRodNetPrice entity = mapper.toEntity(request);
        HyperionRodNetPrice saved = repository.save(entity);
        log.info("Successfully created rod net price with id: {}", saved.getId());

        return mapper.toResponse(saved);
    }

    @Override
    @LoggableAction("UPDATE_ROD_NET_PRICE")
    public HyperionRodNetPriceResponse update(UUID id, HyperionRodNetPriceRequest request) {
        log.debug("Updating rod net price with id: {}", id);

        HyperionRodNetPrice entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate chart entry not found with id: " + id));

        if (!entity.getItem().equalsIgnoreCase(request.getItem()) &&
                repository.existsByItemIgnoreCase(request.getItem())) {
            log.warn("Duplicate entry during update: item={}", request.getItem());
            throw new BusinessException("Another item with this name already exists");
        }

        entity.setItem(request.getItem());
        entity.setK40ufH10f(request.getK40ufH10f());
        entity.setAm70Dm80(request.getAm70Dm80());
        entity.setPn90(request.getPn90());
        entity.setGp10K10f(request.getGp10K10f());

        HyperionRodNetPrice updated = repository.save(entity);
        log.info("Successfully updated rod net price with id: {}", id);

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HyperionRodNetPriceResponse> getAllActive() {
        log.debug("Fetching all active rod net prices");
        List<HyperionRodNetPrice> entities = repository.findByActiveTrue();
        log.info("Found {} active rod net prices", entities.size());
        return mapper.toResponseList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HyperionRodNetPriceResponse> getAllActivePaginated(Pageable pageable) {
        log.debug("Fetching paginated active rod net prices: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<HyperionRodNetPrice> page = repository.findByActiveTrue(pageable);
        log.info("Found {} active rod net prices in page {} of {}",
                page.getNumberOfElements(), page.getNumber(), page.getTotalPages());
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public HyperionRodNetPriceResponse getById(UUID id) {
        log.debug("Fetching rod net price by id: {}", id);
        HyperionRodNetPrice entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate chart entry not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    @LoggableAction("DEACTIVATE_ROD_NET_PRICE")
    public void deactivate(UUID id) {
        log.debug("Deactivating rod net price with id: {}", id);
        HyperionRodNetPrice entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate chart entry not found with id: " + id));
        entity.setActive(false);
        repository.save(entity);
        log.info("Successfully deactivated rod net price with id: {}", id);
    }

    @Override
    @LoggableAction("IMPORT_ROD_NET_PRICE")
    public int importExcel(InputStream inputStream) {
        log.info("Starting Excel import for rod net prices");

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new BusinessException("The Excel sheet is empty");
            }

            // Find column indices with flexible matching
            int itemCol = findColumnIndex(headerRow, "item");
            int k40ufCol = findFlexibleColumnIndex(headerRow, "k40uf", "h10f");
            int am70Col = findFlexibleColumnIndex(headerRow, "am70", "dm80");
            int pn90Col = findColumnIndex(headerRow, "pn90");
            int gp10Col = findFlexibleColumnIndex(headerRow, "gp10", "k10f");

            if (itemCol == -1 || k40ufCol == -1 || am70Col == -1 || pn90Col == -1 || gp10Col == -1) {
                throw new BusinessException("Required columns not found in Excel sheet. " +
                        "Please ensure columns: Item, K40UF/H10F, AM70/DM80, PN90, GP10/K10F are present.");
            }

            int count = 0;
            int lastRowNum = sheet.getLastRowNum();

            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String item = getCellValueAsString(row.getCell(itemCol)).trim();

                // Skip empty rows
                if (item.isEmpty()) {
                    continue;
                }

                BigDecimal k40uf = BigDecimal.valueOf(getCellValueAsDouble(row.getCell(k40ufCol)));
                BigDecimal am70 = BigDecimal.valueOf(getCellValueAsDouble(row.getCell(am70Col)));
                BigDecimal pn90 = BigDecimal.valueOf(getCellValueAsDouble(row.getCell(pn90Col)));
                BigDecimal gp10 = BigDecimal.valueOf(getCellValueAsDouble(row.getCell(gp10Col)));

                // Validate prices
                if (k40uf.compareTo(BigDecimal.ZERO) <= 0 || am70.compareTo(BigDecimal.ZERO) <= 0 ||
                        pn90.compareTo(BigDecimal.ZERO) <= 0 || gp10.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("All prices must be greater than 0 at row " + (r + 1));
                }

                Optional<HyperionRodNetPrice> existingOpt = repository.findByItemIgnoreCase(item);
                HyperionRodNetPrice entity;

                if (existingOpt.isPresent()) {
                    entity = existingOpt.get();
                    entity.setK40ufH10f(k40uf);
                    entity.setAm70Dm80(am70);
                    entity.setPn90(pn90);
                    entity.setGp10K10f(gp10);
                    entity.setActive(true);
                    log.debug("Updating existing entry: item={}", item);
                } else {
                    entity = HyperionRodNetPrice.builder()
                            .item(item)
                            .k40ufH10f(k40uf)
                            .am70Dm80(am70)
                            .pn90(pn90)
                            .gp10K10f(gp10)
                            .active(true)
                            .build();
                    log.debug("Creating new entry: item={}", item);
                }

                repository.save(entity);
                count++;
            }

            log.info("Successfully imported {} rod net prices", count);
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

    private int findFlexibleColumnIndex(Row headerRow, String... keywords) {
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String headerValue = getCellValueAsString(cell).toLowerCase().trim();
                for (String keyword : keywords) {
                    if (headerValue.contains(keyword.toLowerCase())) {
                        return i;
                    }
                }
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