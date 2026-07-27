package com.kalibyte.YashTools.report.service;

import com.kalibyte.YashTools.common.enums.OrderType;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.service.CompanyBrandingService;
import com.kalibyte.YashTools.master.coating.entity.Coating;
import com.kalibyte.YashTools.master.coating.repository.CoatingRepository;
import com.kalibyte.YashTools.production.execution.entity.ExecutionLog;
import com.kalibyte.YashTools.production.execution.repository.ExecutionLogRepository;
import com.kalibyte.YashTools.production.jobcard.repository.JobCardRepository;
import com.kalibyte.YashTools.production.planning.entity.enums.ShiftType;
import com.kalibyte.YashTools.production.tracking.entity.QualityInspection;
import com.kalibyte.YashTools.production.tracking.entity.enums.InspectionResult;
import com.kalibyte.YashTools.production.tracking.repository.QualityInspectionRepository;
import com.kalibyte.YashTools.sales.invoice.entity.SalesInvoice;
import com.kalibyte.YashTools.sales.invoice.repository.SalesInvoiceRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoice;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.repository.PurchaseInvoiceRepository;
import com.kalibyte.YashTools.labors.attendance.entity.Attendance;
import com.kalibyte.YashTools.labors.attendance.repository.AttendanceRepository;
import com.kalibyte.YashTools.report.dto.*;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductionReportService {

    private final ExecutionLogRepository executionLogRepository;
    private final QualityInspectionRepository qualityInspectionRepository;
    private final JobCardRepository jobCardRepository;
    private final CoatingRepository coatingRepository;
    private final CompanyBrandingService companyBrandingService;
    private final TemplateEngine pdfTemplateEngine;

    // Added repositories for P&L report
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final AttendanceRepository attendanceRepository;

    public ProductionReportService(
            ExecutionLogRepository executionLogRepository,
            QualityInspectionRepository qualityInspectionRepository,
            JobCardRepository jobCardRepository,
            CoatingRepository coatingRepository,
            CompanyBrandingService companyBrandingService,
            @Qualifier("pdfTemplateEngine") TemplateEngine pdfTemplateEngine,
            SalesInvoiceRepository salesInvoiceRepository,
            PurchaseInvoiceRepository purchaseInvoiceRepository,
            AttendanceRepository attendanceRepository) {
        this.executionLogRepository = executionLogRepository;
        this.qualityInspectionRepository = qualityInspectionRepository;
        this.jobCardRepository = jobCardRepository;
        this.coatingRepository = coatingRepository;
        this.companyBrandingService = companyBrandingService;
        this.pdfTemplateEngine = pdfTemplateEngine;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(readOnly = true)
    public ProductionExecutionReportDTO getExecutionReport(
            LocalDate startDate, LocalDate endDate,
            Long operatorId, UUID machineId, ShiftType shift,
            String periodLabel) {

        UUID companyId = CompanyContextHolder.getCompanyId();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        Specification<ExecutionLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("jobCard").get("company").get("id"), companyId));
            predicates.add(cb.between(root.get("startTime"), start, end));

            if (operatorId != null) {
                predicates.add(cb.equal(root.get("operator").get("id"), operatorId));
            }
            if (machineId != null) {
                predicates.add(cb.equal(root.get("machine").get("id"), machineId));
            }
            if (shift != null) {
                predicates.add(cb.equal(root.get("shift"), shift));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<ExecutionLog> logs = executionLogRepository.findAll(spec);

        int totalTarget = 0;
        int totalProduced = 0;
        int totalRejected = 0;
        int totalRework = 0;
        int totalDowntime = 0;

        List<ProductionExecutionDetailDTO> details = new ArrayList<>();
        for (ExecutionLog logEntry : logs) {
            totalTarget += logEntry.getTargetQuantity();
            totalProduced += logEntry.getProducedQuantity();
            totalRejected += logEntry.getRejectedQuantity();
            totalRework += logEntry.getReworkQuantity();
            totalDowntime += logEntry.getMachineDowntimeMinutes();

            details.add(ProductionExecutionDetailDTO.builder()
                    .logId(logEntry.getId())
                    .jobCardNo(logEntry.getJobCard().getJobCardNo())
                    .operatorName(logEntry.getOperator().getName())
                    .machineName(logEntry.getMachine().getName())
                    .shift(logEntry.getShift().name())
                    .startTime(logEntry.getStartTime())
                    .endTime(logEntry.getEndTime())
                    .targetQuantity(logEntry.getTargetQuantity())
                    .producedQuantity(logEntry.getProducedQuantity())
                    .rejectedQuantity(logEntry.getRejectedQuantity())
                    .reworkQuantity(logEntry.getReworkQuantity())
                    .downtimeMinutes(logEntry.getMachineDowntimeMinutes())
                    .downtimeReason(logEntry.getDowntimeReason())
                    .build());
        }

        return ProductionExecutionReportDTO.builder()
                .periodLabel(periodLabel)
                .totalTargetQuantity(totalTarget)
                .totalProducedQuantity(totalProduced)
                .totalRejectedQuantity(totalRejected)
                .totalReworkQuantity(totalRework)
                .totalDowntimeMinutes(totalDowntime)
                .details(details)
                .build();
    }

    @Transactional(readOnly = true)
    public QualityInspectionReportDTO getQualityReport(
            LocalDate startDate, LocalDate endDate,
            String inspector, InspectionResult result,
            String periodLabel) {

        UUID companyId = CompanyContextHolder.getCompanyId();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        Specification<QualityInspection> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("company").get("id"), companyId));
            predicates.add(cb.between(root.get("inspectionDate"), start, end));

            if (inspector != null && !inspector.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("inspector")), "%" + inspector.toLowerCase() + "%"));
            }
            if (result != null) {
                predicates.add(cb.equal(root.get("result"), result));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<QualityInspection> inspections = qualityInspectionRepository.findAll(spec);

        long totalInspected = inspections.size();
        int totalAccepted = 0;
        int totalRejected = 0;
        int totalRework = 0;

        List<QualityInspectionDetailDTO> details = new ArrayList<>();
        for (QualityInspection qi : inspections) {
            totalAccepted += qi.getAcceptedQuantity();
            totalRejected += qi.getRejectedQuantity();
            totalRework += qi.getReworkQuantity();

            details.add(QualityInspectionDetailDTO.builder()
                    .inspectionId(qi.getId())
                    .jobCardNo(qi.getJobCard().getJobCardNo())
                    .inspectionDate(qi.getInspectionDate())
                    .inspector(qi.getInspector())
                    .acceptedQuantity(qi.getAcceptedQuantity())
                    .rejectedQuantity(qi.getRejectedQuantity())
                    .reworkQuantity(qi.getReworkQuantity())
                    .result(qi.getResult().name())
                    .remarks(qi.getRemarks())
                    .build());
        }

        int totalQuantity = totalAccepted + totalRejected + totalRework;
        double passRate = totalQuantity > 0 ? (totalAccepted * 100.0 / totalQuantity) : 0.0;
        double rejectRate = totalQuantity > 0 ? (totalRejected * 100.0 / totalQuantity) : 0.0;
        double reworkRate = totalQuantity > 0 ? (totalRework * 100.0 / totalQuantity) : 0.0;

        return QualityInspectionReportDTO.builder()
                .periodLabel(periodLabel)
                .totalInspectedCount(totalInspected)
                .totalAcceptedQuantity(totalAccepted)
                .totalRejectedQuantity(totalRejected)
                .totalReworkQuantity(totalRework)
                .passRate(passRate)
                .rejectRate(rejectRate)
                .reworkRate(reworkRate)
                .details(details)
                .build();
    }

    @Transactional(readOnly = true)
    public CoatingUtilizationReportDTO getCoatingUtilizationReport(
            LocalDate startDate, LocalDate endDate,
            String periodLabel) {

        UUID companyId = CompanyContextHolder.getCompanyId();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Coating> coatings = coatingRepository.findByActiveTrue();
        Map<String, Coating> coatingMapByName = coatings.stream()
                .collect(Collectors.toMap(c -> c.getName().toUpperCase(), c -> c, (c1, c2) -> c1));
        Map<String, Coating> coatingMapByType = coatings.stream()
                .collect(Collectors.toMap(c -> c.getCoatingType().name().toUpperCase(), c -> c, (c1, c2) -> c1));

        Specification<ExecutionLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("jobCard").get("company").get("id"), companyId));
            predicates.add(cb.between(root.get("startTime"), start, end));
            predicates.add(cb.equal(root.get("jobCard").get("workOrderItem").get("coatingRequired"), true));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<ExecutionLog> logs = executionLogRepository.findAll(spec);

        Map<String, List<ExecutionLog>> grouped = logs.stream()
                .filter(logEntry -> logEntry.getJobCard().getWorkOrderItem().getCoatingType() != null)
                .collect(Collectors.groupingBy(logEntry -> logEntry.getJobCard().getWorkOrderItem().getCoatingType().toUpperCase()));

        int overallQuantity = 0;
        BigDecimal overallEstimatedCost = BigDecimal.ZERO;
        List<CoatingUtilizationDetailDTO> details = new ArrayList<>();

        for (Map.Entry<String, List<ExecutionLog>> entry : grouped.entrySet()) {
            String cType = entry.getKey();
            List<ExecutionLog> groupLogs = entry.getValue();

            int coatedQty = groupLogs.stream().mapToInt(ExecutionLog::getProducedQuantity).sum();
            long jcCount = groupLogs.stream().map(l -> l.getJobCard().getId()).distinct().count();

            BigDecimal rate = BigDecimal.ZERO;
            String cName = cType;
            Coating match = coatingMapByName.get(cType);
            if (match == null) {
                match = coatingMapByType.get(cType);
            }

            if (match != null) {
                rate = BigDecimal.valueOf(match.getRate());
                cName = match.getName();
            }

            BigDecimal estCost = rate.multiply(BigDecimal.valueOf(coatedQty));

            overallQuantity += coatedQty;
            overallEstimatedCost = overallEstimatedCost.add(estCost);

            details.add(CoatingUtilizationDetailDTO.builder()
                    .coatingType(cType)
                    .coatingName(cName)
                    .jobCardCount(jcCount)
                    .coatedQuantity(coatedQty)
                    .rate(rate)
                    .estimatedCost(estCost)
                    .build());
        }

        return CoatingUtilizationReportDTO.builder()
                .periodLabel(periodLabel)
                .totalCoatedQuantity(overallQuantity)
                .totalEstimatedCost(overallEstimatedCost)
                .details(details)
                .build();
    }

    @Transactional(readOnly = true)
    public ProductionEfficiencyReportDTO getProductionEfficiencyReport(
            LocalDate startDate, LocalDate endDate,
            String periodLabel) {

        UUID companyId = CompanyContextHolder.getCompanyId();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        Specification<ExecutionLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("jobCard").get("company").get("id"), companyId));
            predicates.add(cb.between(root.get("startTime"), start, end));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<ExecutionLog> logs = executionLogRepository.findAll(spec);

        Map<OrderType, List<ExecutionLog>> grouped = logs.stream()
                .filter(logEntry -> logEntry.getJobCard().getWorkOrderItem().getOrderType() != null)
                .collect(Collectors.groupingBy(logEntry -> logEntry.getJobCard().getWorkOrderItem().getOrderType()));

        int totalTarget = 0;
        int totalProduced = 0;
        List<ProductionEfficiencyDetailDTO> details = new ArrayList<>();

        for (OrderType type : OrderType.values()) {
            List<ExecutionLog> typeLogs = grouped.getOrDefault(type, Collections.emptyList());
            long jcCount = typeLogs.stream().map(l -> l.getJobCard().getId()).distinct().count();
            int targetQty = typeLogs.stream().mapToInt(ExecutionLog::getTargetQuantity).sum();
            int producedQty = typeLogs.stream().mapToInt(ExecutionLog::getProducedQuantity).sum();
            int rejectedQty = typeLogs.stream().mapToInt(ExecutionLog::getRejectedQuantity).sum();

            totalTarget += targetQty;
            totalProduced += producedQty;

            double efficiency = targetQty > 0 ? (producedQty * 100.0 / targetQty) : 0.0;

            details.add(ProductionEfficiencyDetailDTO.builder()
                    .orderType(type.name())
                    .jobCardCount(jcCount)
                    .targetQuantity(targetQty)
                    .producedQuantity(producedQty)
                    .rejectedQuantity(rejectedQty)
                    .efficiency(efficiency)
                    .build());
        }

        double overallEfficiency = totalTarget > 0 ? (totalProduced * 100.0 / totalTarget) : 0.0;

        return ProductionEfficiencyReportDTO.builder()
                .periodLabel(periodLabel)
                .overallEfficiency(overallEfficiency)
                .details(details)
                .build();
    }

    // New Profit & Loss report logic
    @Transactional(readOnly = true)
    public ProfitLossReportDTO getProfitLossReport(LocalDate startDate, LocalDate endDate, String periodLabel) {
        UUID companyId = CompanyContextHolder.getCompanyId();

        // 1. Sales Revenue
        Specification<SalesInvoice> salesSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("company").get("id"), companyId));
            predicates.add(cb.between(root.get("invoiceDate"), startDate, endDate));
            predicates.add(cb.notEqual(root.get("status"), "CANCELLED"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<SalesInvoice> salesInvoices = salesInvoiceRepository.findAll(salesSpec);

        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal cgstSum = BigDecimal.ZERO;
        BigDecimal sgstSum = BigDecimal.ZERO;
        BigDecimal igstSum = BigDecimal.ZERO;

        for (SalesInvoice si : salesInvoices) {
            revenue = revenue.add(si.getSubTotal() != null ? si.getSubTotal() : BigDecimal.ZERO);
            cgstSum = cgstSum.add(si.getCgstAmount() != null ? si.getCgstAmount() : BigDecimal.ZERO);
            sgstSum = sgstSum.add(si.getSgstAmount() != null ? si.getSgstAmount() : BigDecimal.ZERO);
            igstSum = igstSum.add(si.getIgstAmount() != null ? si.getIgstAmount() : BigDecimal.ZERO);
        }
        BigDecimal taxRevenue = cgstSum.add(sgstSum).add(igstSum);
        BigDecimal totalSalesValue = revenue.add(taxRevenue);

        // 2. Purchase / Material Cost
        Specification<PurchaseInvoice> purchaseSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("company").get("id"), companyId));
            predicates.add(cb.between(root.get("invoiceDate"), startDate, endDate));
            predicates.add(cb.notEqual(root.get("status"), com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus.CANCELLED));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<PurchaseInvoice> purchaseInvoices = purchaseInvoiceRepository.findAll(purchaseSpec);

        BigDecimal materialPurchaseCost = BigDecimal.ZERO;
        BigDecimal freightExpenses = BigDecimal.ZERO;
        BigDecimal otherExpenses = BigDecimal.ZERO;

        for (PurchaseInvoice pi : purchaseInvoices) {
            materialPurchaseCost = materialPurchaseCost.add(pi.getTotalAmount() != null ? pi.getTotalAmount() : BigDecimal.ZERO);
            freightExpenses = freightExpenses.add(pi.getFreight() != null ? pi.getFreight() : BigDecimal.ZERO);
            otherExpenses = otherExpenses.add(pi.getOtherCharges() != null ? pi.getOtherCharges() : BigDecimal.ZERO);
        }

        // 3. Labor Cost
        List<Attendance> attendances = attendanceRepository.findByWorkDateBetween(startDate, endDate);
        BigDecimal laborCost = attendances.stream()
                .map(Attendance::getEarnedAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = materialPurchaseCost.add(laborCost).add(freightExpenses).add(otherExpenses);
        BigDecimal netProfit = revenue.subtract(totalExpenses);

        double margin = 0.0;
        if (revenue.compareTo(BigDecimal.ZERO) > 0) {
            margin = netProfit.multiply(BigDecimal.valueOf(100))
                    .divide(revenue, 2, RoundingMode.HALF_UP).doubleValue();
        }

        return ProfitLossReportDTO.builder()
                .periodLabel(periodLabel)
                .revenue(revenue)
                .taxRevenue(taxRevenue)
                .totalSalesValue(totalSalesValue)
                .materialPurchaseCost(materialPurchaseCost)
                .laborCost(laborCost)
                .freightExpenses(freightExpenses)
                .otherExpenses(otherExpenses)
                .totalExpenses(totalExpenses)
                .netProfit(netProfit)
                .netProfitMargin(margin)
                .build();
    }

    public byte[] generatePdfReport(String reportType, Object reportData) throws Exception {
        UUID companyId = CompanyContextHolder.getCompanyId();
        Company company = null;
        try {
            if (companyId != null) {
                company = companyBrandingService.getBrandingForCompany(companyId);
            }
        } catch (Exception e) {
            log.warn("Company branding loading failed: {}", e.getMessage());
        }

        Context context = new Context();
        context.setVariable("reportType", reportType);
        context.setVariable("data", reportData);
        context.setVariable("company", company);
        context.setVariable("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        String html = pdfTemplateEngine.process("report_pdf", context);
        return convertHtmlToPdf(html);
    }

    private byte[] convertHtmlToPdf(String html) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocument(doc, "http://localhost/");
        renderer.layout();
        renderer.createPDF(baos);

        baos.close();
        return baos.toByteArray();
    }

    public byte[] exportExecutionReportToExcel(ProductionExecutionReportDTO report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Production Execution Report");

            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Production Execution Report - " + report.getPeriodLabel());
            titleRow.getCell(0).setCellStyle(boldStyle);

            Row headerRow = sheet.createRow(2);
            String[] columns = {"Job Card No", "Operator", "Machine", "Shift", "Start Time", "End Time", "Target Qty", "Produced Qty", "Rejected Qty", "Rework Qty", "Downtime (min)", "Downtime Reason"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 3;
            for (ProductionExecutionDetailDTO detail : report.getDetails()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(detail.getJobCardNo());
                row.createCell(1).setCellValue(detail.getOperatorName());
                row.createCell(2).setCellValue(detail.getMachineName());
                row.createCell(3).setCellValue(detail.getShift());
                row.createCell(4).setCellValue(detail.getStartTime().toString());
                row.createCell(5).setCellValue(detail.getEndTime() != null ? detail.getEndTime().toString() : "Running");
                row.createCell(6).setCellValue(detail.getTargetQuantity());
                row.createCell(7).setCellValue(detail.getProducedQuantity());
                row.createCell(8).setCellValue(detail.getRejectedQuantity());
                row.createCell(9).setCellValue(detail.getReworkQuantity());
                row.createCell(10).setCellValue(detail.getDowntimeMinutes());
                row.createCell(11).setCellValue(detail.getDowntimeReason() != null ? detail.getDowntimeReason() : "-");
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TOTALS");
            totalLabel.setCellStyle(boldStyle);

            totalRow.createCell(6).setCellValue(report.getTotalTargetQuantity());
            totalRow.getCell(6).setCellStyle(boldStyle);
            totalRow.createCell(7).setCellValue(report.getTotalProducedQuantity());
            totalRow.getCell(7).setCellStyle(boldStyle);
            totalRow.createCell(8).setCellValue(report.getTotalRejectedQuantity());
            totalRow.getCell(8).setCellStyle(boldStyle);
            totalRow.createCell(9).setCellValue(report.getTotalReworkQuantity());
            totalRow.getCell(9).setCellStyle(boldStyle);
            totalRow.createCell(10).setCellValue(report.getTotalDowntimeMinutes());
            totalRow.getCell(10).setCellStyle(boldStyle);

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportQualityReportToExcel(QualityInspectionReportDTO report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Quality Inspection Report");

            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Quality Inspection Report - " + report.getPeriodLabel());
            titleRow.getCell(0).setCellStyle(boldStyle);

            Row headerRow = sheet.createRow(2);
            String[] columns = {"Job Card No", "Inspection Date", "Inspector", "Accepted Qty", "Rejected Qty", "Rework Qty", "Result", "Remarks"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 3;
            for (QualityInspectionDetailDTO detail : report.getDetails()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(detail.getJobCardNo());
                row.createCell(1).setCellValue(detail.getInspectionDate().toString());
                row.createCell(2).setCellValue(detail.getInspector());
                row.createCell(3).setCellValue(detail.getAcceptedQuantity());
                row.createCell(4).setCellValue(detail.getRejectedQuantity());
                row.createCell(5).setCellValue(detail.getReworkQuantity());
                row.createCell(6).setCellValue(detail.getResult());
                row.createCell(7).setCellValue(detail.getRemarks() != null ? detail.getRemarks() : "-");
            }

            Row metricsRow1 = sheet.createRow(rowIdx + 2);
            metricsRow1.createCell(0).setCellValue("Total Inspected Job Cards");
            metricsRow1.getCell(0).setCellStyle(boldStyle);
            metricsRow1.createCell(1).setCellValue(report.getTotalInspectedCount());

            metricsRow1.createCell(3).setCellValue("Pass Rate (%)");
            metricsRow1.getCell(3).setCellStyle(boldStyle);
            metricsRow1.createCell(4).setCellValue(report.getPassRate());

            Row metricsRow2 = sheet.createRow(rowIdx + 3);
            metricsRow2.createCell(0).setCellValue("Total Accepted Quantity");
            metricsRow2.getCell(0).setCellStyle(boldStyle);
            metricsRow2.createCell(1).setCellValue(report.getTotalAcceptedQuantity());

            metricsRow2.createCell(3).setCellValue("Reject Rate (%)");
            metricsRow2.getCell(3).setCellStyle(boldStyle);
            metricsRow2.createCell(4).setCellValue(report.getRejectRate());

            Row metricsRow3 = sheet.createRow(rowIdx + 4);
            metricsRow3.createCell(0).setCellValue("Total Rejected Quantity");
            metricsRow3.getCell(0).setCellStyle(boldStyle);
            metricsRow3.createCell(1).setCellValue(report.getTotalRejectedQuantity());

            metricsRow3.createCell(3).setCellValue("Rework Rate (%)");
            metricsRow3.getCell(3).setCellStyle(boldStyle);
            metricsRow3.createCell(4).setCellValue(report.getReworkRate());

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportCoatingReportToExcel(CoatingUtilizationReportDTO report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Coating Utilization Report");

            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Coating Utilization Report - " + report.getPeriodLabel());
            titleRow.getCell(0).setCellStyle(boldStyle);

            Row headerRow = sheet.createRow(2);
            String[] columns = {"Coating Type", "Coating Name", "Job Cards Processed", "Total Coated Quantity", "Rate", "Estimated Value / Cost"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 3;
            for (CoatingUtilizationDetailDTO detail : report.getDetails()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(detail.getCoatingType());
                row.createCell(1).setCellValue(detail.getCoatingName());
                row.createCell(2).setCellValue(detail.getJobCardCount());
                row.createCell(3).setCellValue(detail.getCoatedQuantity());
                row.createCell(4).setCellValue(detail.getRate().doubleValue());
                row.createCell(5).setCellValue(detail.getEstimatedCost().doubleValue());
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TOTALS");
            totalLabel.setCellStyle(boldStyle);

            totalRow.createCell(3).setCellValue(report.getTotalCoatedQuantity());
            totalRow.getCell(3).setCellStyle(boldStyle);
            totalRow.createCell(5).setCellValue(report.getTotalEstimatedCost().doubleValue());
            totalRow.getCell(5).setCellStyle(boldStyle);

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportEfficiencyReportToExcel(ProductionEfficiencyReportDTO report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Production Efficiency Report");

            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Production Efficiency Report - " + report.getPeriodLabel());
            titleRow.getCell(0).setCellStyle(boldStyle);

            Row headerRow = sheet.createRow(2);
            String[] columns = {"Order Type", "Job Card Count", "Target Quantity", "Produced Quantity", "Rejected Quantity", "Efficiency (%)"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 3;
            for (ProductionEfficiencyDetailDTO detail : report.getDetails()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(detail.getOrderType());
                row.createCell(1).setCellValue(detail.getJobCardCount());
                row.createCell(2).setCellValue(detail.getTargetQuantity());
                row.createCell(3).setCellValue(detail.getProducedQuantity());
                row.createCell(4).setCellValue(detail.getRejectedQuantity());
                row.createCell(5).setCellValue(detail.getEfficiency());
            }

            Row totalRow = sheet.createRow(rowIdx + 2);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("OVERALL SYSTEM EFFICIENCY");
            totalLabel.setCellStyle(boldStyle);

            totalRow.createCell(5).setCellValue(report.getOverallEfficiency());
            totalRow.getCell(5).setCellStyle(boldStyle);

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportProfitLossReportToExcel(ProfitLossReportDTO report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Profit & Loss Report");

            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Profit & Loss Report - " + report.getPeriodLabel());
            titleRow.getCell(0).setCellStyle(boldStyle);

            // Structure of P&L Table
            int rowIdx = 2;
            
            // Section 1: Revenue
            Row rRow = sheet.createRow(rowIdx++);
            rRow.createCell(0).setCellValue("Sales Revenue (Net)");
            rRow.getCell(0).setCellStyle(boldStyle);
            rRow.createCell(1).setCellValue(report.getRevenue().doubleValue());
            
            Row trRow = sheet.createRow(rowIdx++);
            trRow.createCell(0).setCellValue("GST Collected (Output)");
            trRow.createCell(1).setCellValue(report.getTaxRevenue().doubleValue());

            Row tsRow = sheet.createRow(rowIdx++);
            tsRow.createCell(0).setCellValue("Gross Sales (incl. Taxes)");
            tsRow.getCell(0).setCellStyle(boldStyle);
            tsRow.createCell(1).setCellValue(report.getTotalSalesValue().doubleValue());

            rowIdx++; // Spacer

            // Section 2: Expenses
            Row exRow = sheet.createRow(rowIdx++);
            exRow.createCell(0).setCellValue("EXPENSES");
            exRow.getCell(0).setCellStyle(headerCellStyle);

            Row pcRow = sheet.createRow(rowIdx++);
            pcRow.createCell(0).setCellValue("Material Purchase Cost");
            pcRow.createCell(1).setCellValue(report.getMaterialPurchaseCost().doubleValue());

            Row lcRow = sheet.createRow(rowIdx++);
            lcRow.createCell(0).setCellValue("Labor & Wage Expenses");
            lcRow.createCell(1).setCellValue(report.getLaborCost().doubleValue());

            Row frRow = sheet.createRow(rowIdx++);
            frRow.createCell(0).setCellValue("Freight & Shipping Cost");
            frRow.createCell(1).setCellValue(report.getFreightExpenses().doubleValue());

            Row otRow = sheet.createRow(rowIdx++);
            otRow.createCell(0).setCellValue("Other Charges & Overhead");
            otRow.createCell(1).setCellValue(report.getOtherExpenses().doubleValue());

            Row teRow = sheet.createRow(rowIdx++);
            teRow.createCell(0).setCellValue("Total Expenses");
            teRow.getCell(0).setCellStyle(boldStyle);
            teRow.createCell(1).setCellValue(report.getTotalExpenses().doubleValue());
            teRow.getCell(1).setCellStyle(boldStyle);

            rowIdx++; // Spacer

            // Section 3: Profits
            Row npRow = sheet.createRow(rowIdx++);
            npRow.createCell(0).setCellValue("Net Profit / Loss");
            npRow.getCell(0).setCellStyle(headerCellStyle);
            npRow.createCell(1).setCellValue(report.getNetProfit().doubleValue());
            npRow.getCell(1).setCellStyle(boldStyle);

            Row pmRow = sheet.createRow(rowIdx++);
            pmRow.createCell(0).setCellValue("Net Profit Margin (%)");
            pmRow.getCell(0).setCellStyle(boldStyle);
            pmRow.createCell(1).setCellValue(report.getNetProfitMargin() + "%");
            pmRow.getCell(1).setCellStyle(boldStyle);

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
