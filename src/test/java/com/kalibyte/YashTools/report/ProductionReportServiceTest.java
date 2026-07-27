package com.kalibyte.YashTools.report;

import com.kalibyte.YashTools.common.enums.OrderType;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.company.service.CompanyBrandingService;
import com.kalibyte.YashTools.master.coating.entity.Coating;
import com.kalibyte.YashTools.master.coating.repository.CoatingRepository;
import com.kalibyte.YashTools.production.execution.entity.ExecutionLog;
import com.kalibyte.YashTools.production.execution.repository.ExecutionLogRepository;
import com.kalibyte.YashTools.production.jobcard.entity.JobCard;
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
import com.kalibyte.YashTools.report.service.ProductionReportService;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import com.kalibyte.YashTools.labors.labor.entity.Laborer;
import com.kalibyte.YashTools.production.machine.entity.Machine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;
import org.thymeleaf.TemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductionReportServiceTest {

    @Mock
    private ExecutionLogRepository executionLogRepository;

    @Mock
    private QualityInspectionRepository qualityInspectionRepository;

    @Mock
    private JobCardRepository jobCardRepository;

    @Mock
    private CoatingRepository coatingRepository;

    @Mock
    private CompanyBrandingService companyBrandingService;

    @Mock
    private TemplateEngine pdfTemplateEngine;

    @Mock
    private SalesInvoiceRepository salesInvoiceRepository;

    @Mock
    private PurchaseInvoiceRepository purchaseInvoiceRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    private ProductionReportService productionReportService;

    private final UUID companyId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productionReportService = new ProductionReportService(
                executionLogRepository,
                qualityInspectionRepository,
                jobCardRepository,
                coatingRepository,
                companyBrandingService,
                pdfTemplateEngine,
                salesInvoiceRepository,
                purchaseInvoiceRepository,
                attendanceRepository
        );
        CompanyContextHolder.setCompanyId(companyId);
    }

    @Test
    void getExecutionReport_Success() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();

        WorkOrderItem woi = WorkOrderItem.builder().toolName("Carbide Endmill").build();
        JobCard jc = JobCard.builder().jobCardNo("JC-001").workOrderItem(woi).build();
        Laborer laborer = Laborer.builder().name("John Operator").build();
        Machine machine = Machine.builder().name("CNC Machine 1").build();

        ExecutionLog logEntry = ExecutionLog.builder()
                .jobCard(jc)
                .operator(laborer)
                .machine(machine)
                .shift(ShiftType.MORNING)
                .startTime(LocalDateTime.now().minusHours(4))
                .endTime(LocalDateTime.now())
                .targetQuantity(100)
                .producedQuantity(95)
                .rejectedQuantity(3)
                .reworkQuantity(2)
                .machineDowntimeMinutes(10)
                .downtimeReason("Tool replacement")
                .build();

        when(executionLogRepository.findAll(any(Specification.class))).thenReturn(List.of(logEntry));

        ProductionExecutionReportDTO report = productionReportService.getExecutionReport(
                start, end, null, null, null, "TEST_PERIOD");

        assertNotNull(report);
        assertEquals("TEST_PERIOD", report.getPeriodLabel());
        assertEquals(100, report.getTotalTargetQuantity());
        assertEquals(95, report.getTotalProducedQuantity());
        assertEquals(3, report.getTotalRejectedQuantity());
        assertEquals(2, report.getTotalReworkQuantity());
        assertEquals(10, report.getTotalDowntimeMinutes());
        assertEquals(1, report.getDetails().size());
    }

    @Test
    void getQualityReport_Success() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();

        JobCard jc = JobCard.builder().jobCardNo("JC-001").build();
        QualityInspection qi = QualityInspection.builder()
                .jobCard(jc)
                .inspectionDate(LocalDateTime.now())
                .inspector("Alice Inspector")
                .acceptedQuantity(90)
                .rejectedQuantity(5)
                .reworkQuantity(5)
                .result(InspectionResult.PASS)
                .remarks("Within tolerance limits")
                .build();

        when(qualityInspectionRepository.findAll(any(Specification.class))).thenReturn(List.of(qi));

        QualityInspectionReportDTO report = productionReportService.getQualityReport(
                start, end, null, null, "TEST_PERIOD");

        assertNotNull(report);
        assertEquals(1, report.getTotalInspectedCount());
        assertEquals(90, report.getTotalAcceptedQuantity());
        assertEquals(5, report.getTotalRejectedQuantity());
        assertEquals(5, report.getTotalReworkQuantity());
        assertEquals(90.0, report.getPassRate());
        assertEquals(5.0, report.getRejectRate());
        assertEquals(5.0, report.getReworkRate());
    }

    @Test
    void getCoatingReport_Success() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();

        Coating coating = Coating.builder()
                .name("ALCRONA")
                .coatingType(com.kalibyte.YashTools.common.enums.CoatingType.ALCRONA)
                .rate(150.0)
                .active(true)
                .build();

        when(coatingRepository.findByActiveTrue()).thenReturn(List.of(coating));

        WorkOrderItem woi = WorkOrderItem.builder()
                .coatingRequired(true)
                .coatingType("ALCRONA")
                .build();
        JobCard jc = JobCard.builder().jobCardNo("JC-001").workOrderItem(woi).build();
        ExecutionLog logEntry = ExecutionLog.builder()
                .jobCard(jc)
                .startTime(LocalDateTime.now())
                .targetQuantity(10)
                .producedQuantity(10)
                .build();

        when(executionLogRepository.findAll(any(Specification.class))).thenReturn(List.of(logEntry));

        CoatingUtilizationReportDTO report = productionReportService.getCoatingUtilizationReport(
                start, end, "TEST_PERIOD");

        assertNotNull(report);
        assertEquals(10, report.getTotalCoatedQuantity());
        assertEquals(0, BigDecimal.valueOf(1500.0).compareTo(report.getTotalEstimatedCost()));
    }

    @Test
    void getEfficiencyReport_Success() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();

        WorkOrderItem woi = WorkOrderItem.builder()
                .orderType(OrderType.NEW_TOOL)
                .build();
        JobCard jc = JobCard.builder().jobCardNo("JC-001").workOrderItem(woi).build();
        ExecutionLog logEntry = ExecutionLog.builder()
                .jobCard(jc)
                .startTime(LocalDateTime.now())
                .targetQuantity(20)
                .producedQuantity(18)
                .rejectedQuantity(2)
                .build();

        when(executionLogRepository.findAll(any(Specification.class))).thenReturn(List.of(logEntry));

        ProductionEfficiencyReportDTO report = productionReportService.getProductionEfficiencyReport(
                start, end, "TEST_PERIOD");

        assertNotNull(report);
        assertEquals(90.0, report.getOverallEfficiency());
    }

    @Test
    void getProfitLossReport_Success() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();

        SalesInvoice salesInvoice = SalesInvoice.builder()
                .invoiceDate(LocalDate.now())
                .subTotal(BigDecimal.valueOf(10000))
                .cgstAmount(BigDecimal.valueOf(900))
                .sgstAmount(BigDecimal.valueOf(900))
                .igstAmount(BigDecimal.ZERO)
                .status("APPROVED")
                .build();

        PurchaseInvoice purchaseInvoice = PurchaseInvoice.builder()
                .invoiceDate(LocalDate.now())
                .totalAmount(BigDecimal.valueOf(4000))
                .freight(BigDecimal.valueOf(300))
                .otherCharges(BigDecimal.valueOf(200))
                .status(com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus.APPROVED)
                .build();

        Attendance attendance = Attendance.builder()
                .workDate(LocalDate.now())
                .earnedAmount(BigDecimal.valueOf(1500))
                .build();

        when(salesInvoiceRepository.findAll(any(Specification.class))).thenReturn(List.of(salesInvoice));
        when(purchaseInvoiceRepository.findAll(any(Specification.class))).thenReturn(List.of(purchaseInvoice));
        when(attendanceRepository.findByWorkDateBetween(start, end)).thenReturn(List.of(attendance));

        ProfitLossReportDTO report = productionReportService.getProfitLossReport(start, end, "TEST_PERIOD");

        assertNotNull(report);
        assertEquals("TEST_PERIOD", report.getPeriodLabel());
        assertEquals(0, BigDecimal.valueOf(10000).compareTo(report.getRevenue()));
        assertEquals(0, BigDecimal.valueOf(1800).compareTo(report.getTaxRevenue()));
        assertEquals(0, BigDecimal.valueOf(11800).compareTo(report.getTotalSalesValue()));
        assertEquals(0, BigDecimal.valueOf(4000).compareTo(report.getMaterialPurchaseCost()));
        assertEquals(0, BigDecimal.valueOf(1500).compareTo(report.getLaborCost()));
        assertEquals(0, BigDecimal.valueOf(300).compareTo(report.getFreightExpenses()));
        assertEquals(0, BigDecimal.valueOf(200).compareTo(report.getOtherExpenses()));
        assertEquals(0, BigDecimal.valueOf(6000).compareTo(report.getTotalExpenses()));
        assertEquals(0, BigDecimal.valueOf(4000).compareTo(report.getNetProfit()));
        assertEquals(40.0, report.getNetProfitMargin());
    }
}
