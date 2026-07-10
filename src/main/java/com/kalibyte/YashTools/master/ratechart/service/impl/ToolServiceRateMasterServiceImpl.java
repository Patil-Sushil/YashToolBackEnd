package com.kalibyte.YashTools.master.ratechart.service.impl;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.enums.ServiceType;
import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.master.ratechart.dto.request.ToolServiceRateMasterRequest;
import com.kalibyte.YashTools.master.ratechart.dto.response.ToolServiceRateMasterResponse;
import com.kalibyte.YashTools.master.ratechart.entity.ToolServiceRateMaster;
import com.kalibyte.YashTools.master.ratechart.mapper.ToolServiceRateMasterMapper;
import com.kalibyte.YashTools.master.ratechart.repository.ToolServiceRateMasterRepository;
import com.kalibyte.YashTools.master.ratechart.service.ToolServiceRateMasterService;
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
public class ToolServiceRateMasterServiceImpl implements ToolServiceRateMasterService {

    private final ToolServiceRateMasterRepository repository;
    private final ToolServiceRateMasterMapper mapper;

    @Override
    @LoggableAction(value = "Create Tool Service Rate Master", action = AuditAction.TOOL_SERVICE_RATE_MASTER_CREATED, entityType = "RATE_CHART")
    public ToolServiceRateMasterResponse create(ToolServiceRateMasterRequest request) {
        log.debug("Creating tool service rate for: serviceType={}, toolType={}", request.getServiceType(), request.getToolType());

        UUID companyId = CompanyContextHolder.getCompanyId();

        // Auto-generate service code if not provided
        String serviceCode = request.getServiceCode();
        if (serviceCode == null || serviceCode.isBlank()) {
            serviceCode = generateNextServiceCode(companyId);
        } else {
            if (repository.existsByServiceCodeIgnoreCaseAndCompanyId(serviceCode, companyId)) {
                throw new BusinessException("Service code '" + serviceCode + "' already exists for this company");
            }
        }

        ToolServiceRateMaster entity = mapper.toEntity(request);
        entity.setServiceCode(serviceCode);
        ToolServiceRateMaster saved = repository.save(entity);
        log.info("Successfully created tool service rate with serviceCode: {}", saved.getServiceCode());

        return mapper.toResponse(saved);
    }

    @Override
    @LoggableAction(value = "Update Tool Service Rate Master", action = AuditAction.TOOL_SERVICE_RATE_MASTER_UPDATED, entityType = "RATE_CHART")
    public ToolServiceRateMasterResponse update(UUID id, ToolServiceRateMasterRequest request) {
        log.debug("Updating tool service rate with id: {}", id);

        ToolServiceRateMaster entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tool service rate entry not found with id: " + id));

        UUID companyId = CompanyContextHolder.getCompanyId();

        if (request.getServiceCode() != null && !request.getServiceCode().isBlank() &&
                !entity.getServiceCode().equalsIgnoreCase(request.getServiceCode())) {
            if (repository.existsByServiceCodeIgnoreCaseAndCompanyId(request.getServiceCode(), companyId)) {
                throw new BusinessException("Service code '" + request.getServiceCode() + "' already exists for this company");
            }
            entity.setServiceCode(request.getServiceCode());
        }

        entity.setServiceType(request.getServiceType());
        entity.setToolType(request.getToolType());
        entity.setToolMaterial(request.getToolMaterial());
        entity.setDiameterFrom(request.getDiameterFrom());
        entity.setDiameterTo(request.getDiameterTo());
        entity.setNoOfFlutes(request.getNoOfFlutes());
        entity.setProfileType(request.getProfileType());
        entity.setBaseRate(request.getBaseRate());
        entity.setMinorDamageCharge(request.getMinorDamageCharge());
        entity.setMediumDamageCharge(request.getMediumDamageCharge());
        entity.setMajorDamageCharge(request.getMajorDamageCharge());
        entity.setCoatingTiN(request.getCoatingTiN());
        entity.setCoatingTiAlN(request.getCoatingTiAlN());
        entity.setCoatingAlCrN(request.getCoatingAlCrN());
        entity.setCoatingDlc(request.getCoatingDlc());
        entity.setSpecialGeometryCharge(request.getSpecialGeometryCharge());
        entity.setSpecialProfileCharge(request.getSpecialProfileCharge());
        entity.setExpressDeliveryCharge(request.getExpressDeliveryCharge());
        entity.setStandardDeliveryDays(request.getStandardDeliveryDays());
        entity.setActive(request.getActive() != null ? request.getActive() : entity.getActive());

        ToolServiceRateMaster updated = repository.save(entity);
        log.info("Successfully updated tool service rate with id: {}", id);

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ToolServiceRateMasterResponse> getAllActive() {
        log.debug("Fetching all active tool service rates");
        List<ToolServiceRateMaster> entities = repository.findByActiveTrue();
        return mapper.toResponseList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ToolServiceRateMasterResponse> getAllActivePaginated(Pageable pageable) {
        log.debug("Fetching paginated active tool service rates");
        Page<ToolServiceRateMaster> page = repository.findByActiveTrue(pageable);
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ToolServiceRateMasterResponse getById(UUID id) {
        log.debug("Fetching tool service rate by id: {}", id);
        ToolServiceRateMaster entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tool service rate entry not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    @LoggableAction(value = "Deactivate Tool Service Rate Master", action = AuditAction.TOOL_SERVICE_RATE_MASTER_DELETED, entityType = "RATE_CHART")
    public void deactivate(UUID id) {
        log.debug("Deactivating tool service rate with id: {}", id);
        ToolServiceRateMaster entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tool service rate entry not found with id: " + id));
        entity.setActive(false);
        repository.save(entity);
        log.info("Successfully deactivated tool service rate with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ToolServiceRateMaster findMatchingRate(ServiceType serviceType, String toolType, String toolMaterial, Double diameter) {
        log.debug("Matching service rate for serviceType={}, toolType={}, material={}, diameter={}",
                serviceType, toolType, toolMaterial, diameter);
        List<ToolServiceRateMaster> matches = repository.findMatchingRates(serviceType, toolType, toolMaterial, diameter);
        if (matches.isEmpty()) {
            return null;
        }
        return matches.get(0);
    }

    @Override
    @LoggableAction(value = "Import Tool Service Rate Master", action = AuditAction.TOOL_SERVICE_RATE_MASTER_IMPORTED, entityType = "RATE_CHART")
    public int importExcel(InputStream inputStream) {
        log.info("Starting Excel import for tool service rates");

        UUID companyId = CompanyContextHolder.getCompanyId();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new BusinessException("The Excel sheet is empty");
            }

            // Find column indices with flexible matching
            int serviceCol = findFlexibleColumnIndex(headerRow, "service", "service type", "service_type");
            int toolCol = findFlexibleColumnIndex(headerRow, "tool", "tool type", "tool_type");
            int diaFromCol = findFlexibleColumnIndex(headerRow, "dia from", "diameter from", "dia_from", "min dia", "diameter_from");
            int diaToCol = findFlexibleColumnIndex(headerRow, "dia to", "diameter to", "dia_to", "max dia", "diameter_to");
            int materialCol = findFlexibleColumnIndex(headerRow, "material", "tool material", "tool_material");
            int baseRateCol = findFlexibleColumnIndex(headerRow, "base rate", "base rate charge", "base_rate", "rate", "base");
            int minorCol = findFlexibleColumnIndex(headerRow, "minor", "minor damage", "minor_damage");
            int mediumCol = findFlexibleColumnIndex(headerRow, "medium", "medium damage", "medium_damage");
            int majorCol = findFlexibleColumnIndex(headerRow, "major", "major damage", "major_damage");
            int tinCol = findFlexibleColumnIndex(headerRow, "tin");
            int tialnCol = findFlexibleColumnIndex(headerRow, "tialn");
            int alcrnCol = findFlexibleColumnIndex(headerRow, "alcrn");
            int dlcCol = findFlexibleColumnIndex(headerRow, "dlc");
            int specGeomCol = findFlexibleColumnIndex(headerRow, "special geometry", "spec geom", "special_geometry");
            int specProfCol = findFlexibleColumnIndex(headerRow, "special profile", "spec prof", "special_profile");
            int expressCol = findFlexibleColumnIndex(headerRow, "express", "express delivery", "express_delivery");
            int daysCol = findFlexibleColumnIndex(headerRow, "days", "delivery days", "standard delivery days", "standard_delivery_days");

            // Required columns check
            if (serviceCol == -1 || toolCol == -1 || diaFromCol == -1 || diaToCol == -1 || materialCol == -1 || baseRateCol == -1) {
                throw new BusinessException("Required columns not found in Excel sheet. " +
                        "Please ensure columns: Service, Tool, Dia From, Dia To, Material, Base Rate are present.");
            }

            int count = 0;
            int lastRowNum = sheet.getLastRowNum();

            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String serviceStr = getCellValueAsString(row.getCell(serviceCol)).trim();
                String tool = getCellValueAsString(row.getCell(toolCol)).trim();

                // Skip empty rows
                if (serviceStr.isEmpty() || tool.isEmpty()) {
                    continue;
                }

                ServiceType serviceType = ServiceType.fromString(serviceStr);
                if (serviceType == null) {
                    throw new BusinessException("Invalid Service Type '" + serviceStr + "' at row " + (r + 1));
                }

                Double diaFrom = getCellValueAsDouble(row.getCell(diaFromCol));
                Double diaTo = getCellValueAsDouble(row.getCell(diaToCol));
                String material = getCellValueAsString(row.getCell(materialCol)).trim();
                BigDecimal baseRate = BigDecimal.valueOf(getCellValueAsDouble(row.getCell(baseRateCol)));

                BigDecimal minor = minorCol != -1 ? getCellValueAsBigDecimal(row.getCell(minorCol)) : null;
                BigDecimal medium = mediumCol != -1 ? getCellValueAsBigDecimal(row.getCell(mediumCol)) : null;
                BigDecimal major = majorCol != -1 ? getCellValueAsBigDecimal(row.getCell(majorCol)) : null;

                BigDecimal tin = tinCol != -1 ? getCellValueAsBigDecimal(row.getCell(tinCol)) : null;
                BigDecimal tialn = tialnCol != -1 ? getCellValueAsBigDecimal(row.getCell(tialnCol)) : null;
                BigDecimal alcrn = alcrnCol != -1 ? getCellValueAsBigDecimal(row.getCell(alcrnCol)) : null;
                BigDecimal dlc = dlcCol != -1 ? getCellValueAsBigDecimal(row.getCell(dlcCol)) : null;

                BigDecimal specGeom = specGeomCol != -1 ? getCellValueAsBigDecimal(row.getCell(specGeomCol)) : null;
                BigDecimal specProf = specProfCol != -1 ? getCellValueAsBigDecimal(row.getCell(specProfCol)) : null;
                BigDecimal express = expressCol != -1 ? getCellValueAsBigDecimal(row.getCell(expressCol)) : null;

                int days = daysCol != -1 ? getCellValueAsDouble(row.getCell(daysCol)).intValue() : 5; // Default 5 days if not specified
                if (days <= 0) days = 5;

                // Lookup existing rate by (serviceType, toolType, toolMaterial, diameterFrom, diameterTo)
                // We search using our repository
                List<ToolServiceRateMaster> existing = repository.findMatchingRates(serviceType, tool, material, diaFrom);
                Optional<ToolServiceRateMaster> existingOpt = existing.stream()
                        .filter(e -> e.getDiameterFrom().equals(diaFrom) && e.getDiameterTo().equals(diaTo))
                        .findFirst();

                ToolServiceRateMaster entity;
                if (existingOpt.isPresent()) {
                    entity = existingOpt.get();
                    log.debug("Updating existing service rate record: {}", entity.getServiceCode());
                } else {
                    entity = new ToolServiceRateMaster();
                    entity.setServiceCode(generateNextServiceCode(companyId));
                    log.debug("Creating new service rate record: {}", entity.getServiceCode());
                }

                entity.setServiceType(serviceType);
                entity.setToolType(tool);
                entity.setToolMaterial(material);
                entity.setDiameterFrom(diaFrom);
                entity.setDiameterTo(diaTo);
                entity.setBaseRate(baseRate);
                entity.setMinorDamageCharge(minor);
                entity.setMediumDamageCharge(medium);
                entity.setMajorDamageCharge(major);
                entity.setCoatingTiN(tin);
                entity.setCoatingTiAlN(tialn);
                entity.setCoatingAlCrN(alcrn);
                entity.setCoatingDlc(dlc);
                entity.setSpecialGeometryCharge(specGeom);
                entity.setSpecialProfileCharge(specProf);
                entity.setExpressDeliveryCharge(express);
                entity.setStandardDeliveryDays(days);
                entity.setActive(true);

                repository.save(entity);
                count++;
            }

            log.info("Successfully imported {} tool service rates", count);
            return count;

        } catch (Exception e) {
            log.error("Failed to import Excel file", e);
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException("Failed to import Excel file: " + e.getMessage());
        }
    }

    private String generateNextServiceCode(UUID companyId) {
        String prefix = "SRV-";
        int suffix = 1;
        while (repository.existsByServiceCodeIgnoreCaseAndCompanyId(prefix + String.format("%05d", suffix), companyId)) {
            suffix++;
        }
        return prefix + String.format("%05d", suffix);
    }

    private int findFlexibleColumnIndex(Row headerRow, String... keywords) {
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String headerValue = getCellValueAsString(cell).toLowerCase().trim();
                for (String keyword : keywords) {
                    if (headerValue.equalsIgnoreCase(keyword) || headerValue.contains(keyword.toLowerCase())) {
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
                String val = cell.getStringCellValue().replaceAll("[^\\d.]", "").trim();
                try {
                    yield Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    yield 0.0;
                }
            }
            default -> 0.0;
        };
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) {
            return BigDecimal.ZERO;
        }
        double val = getCellValueAsDouble(cell);
        return BigDecimal.valueOf(val);
    }
}
