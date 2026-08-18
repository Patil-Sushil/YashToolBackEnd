package com.kalibyte.YashTools.workflow;

import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.enquiry.entity.Enquiry;
import com.kalibyte.YashTools.enquiry.entity.enums.EnquiryStatus;
import com.kalibyte.YashTools.enquiry.repository.EnquiryRepository;
import com.kalibyte.YashTools.production.execution.entity.ExecutionLog;
import com.kalibyte.YashTools.production.execution.repository.ExecutionLogRepository;
import com.kalibyte.YashTools.production.finishedgoods.service.FinishedGoodsStockService;
import com.kalibyte.YashTools.production.jobcard.dto.CreateJobCardRequest;
import com.kalibyte.YashTools.production.jobcard.entity.JobCard;
import com.kalibyte.YashTools.production.jobcard.entity.enums.JobCardStatus;
import com.kalibyte.YashTools.production.jobcard.repository.JobCardRepository;
import com.kalibyte.YashTools.production.jobcard.service.impl.JobCardServiceImpl;
import com.kalibyte.YashTools.production.logistics.damage.entity.TransitDamageReport;
import com.kalibyte.YashTools.production.logistics.damage.entity.enums.TransitDamageAction;
import com.kalibyte.YashTools.production.logistics.damage.entity.enums.TransitDamageStatus;
import com.kalibyte.YashTools.production.logistics.damage.repository.TransitDamageReportRepository;
import com.kalibyte.YashTools.production.logistics.damage.service.impl.TransitDamageServiceImpl;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryReceiptRequest;
import com.kalibyte.YashTools.production.logistics.entity.DeliveryChallan;
import com.kalibyte.YashTools.production.logistics.repository.DeliveryChallanItemRepository;
import com.kalibyte.YashTools.production.logistics.repository.DeliveryChallanRepository;
import com.kalibyte.YashTools.production.logistics.service.impl.DeliveryChallanServiceImpl;
import com.kalibyte.YashTools.production.machine.repository.MachineRepository;
import com.kalibyte.YashTools.production.planning.repository.ProductionScheduleRepository;
import com.kalibyte.YashTools.production.tracking.dto.QualityInspectionRequest;
import com.kalibyte.YashTools.production.tracking.entity.QualityInspection;
import com.kalibyte.YashTools.production.tracking.repository.QualityInspectionRepository;
import com.kalibyte.YashTools.production.tracking.service.impl.QualityInspectionServiceImpl;
import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.quotation.entity.QuotationItem;
import com.kalibyte.YashTools.quotation.entity.enums.QuotationStatus;
import com.kalibyte.YashTools.quotation.repository.QuotationRepository;
import com.kalibyte.YashTools.quotation.security.QuotationSecurityService;
import com.kalibyte.YashTools.workorder.dto.request.CreateWorkOrderRequest;
import com.kalibyte.YashTools.workorder.entity.WorkOrder;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import com.kalibyte.YashTools.workorder.entity.enums.WorkOrderStatus;
import com.kalibyte.YashTools.workorder.repository.WorkOrderItemRepository;
import com.kalibyte.YashTools.workorder.repository.WorkOrderRepository;
import com.kalibyte.YashTools.workorder.service.impl.WorkOrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkflowStatusAutoTransitionTest {

    @Mock private WorkOrderRepository workOrderRepository;
    @Mock private QuotationRepository quotationRepository;
    @Mock private QuotationSecurityService quotationSecurityService;
    @Mock private WorkOrderItemRepository workOrderItemRepository;
    @Mock private EnquiryRepository enquiryRepository;
    @Mock private JobCardRepository jobCardRepository;
    @Mock private ExecutionLogRepository executionLogRepository;
    @Mock private QualityInspectionRepository qualityInspectionRepository;
    @Mock private ProductionScheduleRepository scheduleRepository;
    @Mock private MachineRepository machineRepository;
    @Mock private FinishedGoodsStockService finishedGoodsStockService;
    @Mock private DeliveryChallanRepository deliveryChallanRepository;
    @Mock private DeliveryChallanItemRepository deliveryChallanItemRepository;
    @Mock private TransitDamageReportRepository transitDamageReportRepository;

    @Mock private com.kalibyte.YashTools.production.packing.repository.PackingLogRepository packingLogRepository;
    @Mock private com.kalibyte.YashTools.sales.invoice.repository.SalesInvoiceItemRepository salesInvoiceItemRepository;

    private UUID companyId;
    private Company company;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        companyId = UUID.randomUUID();
        CompanyContextHolder.setCompanyId(companyId);
        CompanyContextHolder.setCompanyCode("YT");
        company = Company.builder().id(companyId).code("YT").build();
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    @Test
    void testCreateWorkOrderFromQuotation_MarksSourceEnquiryAsAccepted() {
        WorkOrderServiceImpl workOrderService = new WorkOrderServiceImpl(
                workOrderRepository, quotationRepository, quotationSecurityService,
                workOrderItemRepository, enquiryRepository, jobCardRepository,
                executionLogRepository, qualityInspectionRepository, packingLogRepository,
                deliveryChallanItemRepository, salesInvoiceItemRepository
        );

        UUID qId = UUID.randomUUID();
        com.kalibyte.YashTools.customer.entity.Customer mockCustomer = com.kalibyte.YashTools.customer.entity.Customer.builder()
                .customerName("Apex Customer")
                .companyName("Apex Machining")
                .build();
        mockCustomer.setId(UUID.randomUUID());

        Enquiry sourceEnquiry = Enquiry.builder().status(EnquiryStatus.QUOTED).build();
        Quotation quotation = Quotation.builder()
                .quotationNo("QT-2026-0001")
                .status(QuotationStatus.LOCKED)
                .sourceEnquiry(sourceEnquiry)
                .customer(mockCustomer)
                .customerCompanyName("Apex Machining")
                .items(List.of())
                .build();
        quotation.setId(qId);
        quotation.setCompany(company);

        when(quotationSecurityService.loadForCurrentCompany(qId)).thenReturn(quotation);
        when(workOrderRepository.existsByQuotationId(qId)).thenReturn(false);
        when(workOrderRepository.count()).thenReturn(0L);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setQuotationId(qId);
        req.setExpectedDeliveryDate(LocalDate.now().plusDays(10));

        workOrderService.createFromQuotation(req);

        assertEquals(EnquiryStatus.ACCEPTED, sourceEnquiry.getStatus());
        verify(enquiryRepository, times(1)).save(sourceEnquiry);
    }

    @Test
    void testCreateJobCard_TransitionsWorkOrderFromCreatedToInProgress() {
        JobCardServiceImpl jobCardService = new JobCardServiceImpl(
                jobCardRepository, workOrderItemRepository, workOrderRepository
        );

        UUID itemId = UUID.randomUUID();
        WorkOrder wo = WorkOrder.builder()
                .status(WorkOrderStatus.CREATED)
                .items(new ArrayList<>())
                .build();
        wo.setCompany(company);

        WorkOrderItem item = WorkOrderItem.builder()
                .quantity(10)
                .workOrder(wo)
                .build();
        item.setId(itemId);

        when(workOrderItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(jobCardRepository.getSumQuantityByWorkOrderItemId(itemId, companyId)).thenReturn(0);
        when(jobCardRepository.count()).thenReturn(0L);
        when(jobCardRepository.save(any(JobCard.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateJobCardRequest request = new CreateJobCardRequest();
        request.setWorkOrderItemId(itemId);
        request.setQuantity(5);

        jobCardService.create(request);

        assertEquals(WorkOrderStatus.IN_PROGRESS, wo.getStatus());
        verify(workOrderRepository, times(1)).save(wo);
    }

    @Test
    void testQualityInspectionPass_CompletesJobCardAndCompletesWorkOrderProduction() {
        QualityInspectionServiceImpl qcService = new QualityInspectionServiceImpl(
                qualityInspectionRepository, jobCardRepository, executionLogRepository,
                scheduleRepository, machineRepository, finishedGoodsStockService, workOrderRepository
        );

        UUID jcId = UUID.randomUUID();
        WorkOrder wo = WorkOrder.builder()
                .workOrderNo("WO-001")
                .status(WorkOrderStatus.IN_PROGRESS)
                .items(new ArrayList<>())
                .build();
        wo.setCompany(company);

        WorkOrderItem woi = WorkOrderItem.builder()
                .quantity(5)
                .workOrder(wo)
                .build();
        wo.getItems().add(woi);

        JobCard jc = JobCard.builder()
                .jobCardNo("JC-001")
                .status(JobCardStatus.STARTED)
                .totalQuantity(5)
                .workOrder(wo)
                .workOrderItem(woi)
                .priority(1)
                .build();
        jc.setId(jcId);
        jc.setCompany(company);

        when(jobCardRepository.findByIdAndCompanyId(jcId, companyId)).thenReturn(Optional.of(jc));
        when(qualityInspectionRepository.existsByJobCardId(jcId)).thenReturn(false);
        when(executionLogRepository.findByJobCardId(jcId)).thenReturn(List.of());
        when(qualityInspectionRepository.save(any(QualityInspection.class))).thenAnswer(inv -> inv.getArgument(0));

        QualityInspection qiRecord = QualityInspection.builder().acceptedQuantity(5).build();
        when(jobCardRepository.findByWorkOrderIdAndCompanyId(wo.getId(), companyId)).thenReturn(List.of(jc));
        when(jobCardRepository.findByWorkOrderItemIdAndCompanyId(woi.getId(), companyId)).thenReturn(List.of(jc));
        when(qualityInspectionRepository.findByJobCardIdAndCompanyId(jcId, companyId)).thenReturn(Optional.of(qiRecord));

        QualityInspectionRequest req = new QualityInspectionRequest();
        req.setJobCardId(jcId);
        req.setAcceptedQuantity(5);
        req.setRejectedQuantity(0);
        req.setReworkQuantity(0);
        req.setInspector("Inspector 1");

        qcService.inspect(req);

        assertEquals(JobCardStatus.COMPLETED, jc.getStatus());
        assertEquals(WorkOrderStatus.PRODUCTION_COMPLETED, wo.getStatus());
        verify(workOrderRepository, times(1)).save(wo);
    }

    @Test
    void testDeliveryReceipt_CompletesWorkOrderWhenAllItemsDelivered() {
        DeliveryChallanServiceImpl dcService = new DeliveryChallanServiceImpl(
                deliveryChallanRepository, deliveryChallanItemRepository, workOrderRepository, workOrderItemRepository
        );

        UUID dcId = UUID.randomUUID();
        WorkOrder wo = WorkOrder.builder()
                .status(WorkOrderStatus.PRODUCTION_COMPLETED)
                .items(new ArrayList<>())
                .build();
        wo.setCompany(company);

        WorkOrderItem woi = WorkOrderItem.builder()
                .quantity(10)
                .workOrder(wo)
                .build();
        woi.setId(UUID.randomUUID());
        wo.getItems().add(woi);

        DeliveryChallan dc = DeliveryChallan.builder()
                .status("DISPATCHED")
                .workOrder(wo)
                .build();
        dc.setId(dcId);
        dc.setCompany(company);

        when(deliveryChallanRepository.findByIdAndCompanyId(dcId, companyId)).thenReturn(Optional.of(dc));
        when(deliveryChallanRepository.save(any(DeliveryChallan.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deliveryChallanItemRepository.getSumDeliveredQuantityByWorkOrderItemId(woi.getId())).thenReturn(10);

        DeliveryReceiptRequest req = new DeliveryReceiptRequest();
        req.setReceivedBy("Store Incharge");

        dcService.recordDeliveryReceipt(dcId, req);

        assertEquals("DELIVERED", dc.getStatus());
        assertEquals(WorkOrderStatus.COMPLETED, wo.getStatus());
        verify(workOrderRepository, times(1)).save(wo);
    }

    @Test
    void testTransitDamageApproval_ReopensWorkOrderToInProgress() {
        TransitDamageServiceImpl transitService = new TransitDamageServiceImpl(
                transitDamageReportRepository, deliveryChallanRepository, workOrderItemRepository,
                jobCardRepository, workOrderRepository
        );

        UUID reportId = UUID.randomUUID();
        WorkOrder wo = WorkOrder.builder()
                .workOrderNo("WO-001")
                .status(WorkOrderStatus.COMPLETED)
                .build();
        wo.setCompany(company);

        WorkOrderItem woi = WorkOrderItem.builder()
                .workOrder(wo)
                .build();
        woi.setId(UUID.randomUUID());

        DeliveryChallan challan = DeliveryChallan.builder()
                .challanNo("DC-001")
                .build();

        TransitDamageReport report = TransitDamageReport.builder()
                .status(TransitDamageStatus.REPORTED)
                .action(TransitDamageAction.REPLACE)
                .damagedQuantity(2)
                .workOrderItem(woi)
                .deliveryChallan(challan)
                .build();
        report.setId(reportId);
        report.setCompany(company);

        when(transitDamageReportRepository.findByIdAndCompanyId(reportId, companyId)).thenReturn(Optional.of(report));
        when(jobCardRepository.count()).thenReturn(0L);
        when(jobCardRepository.save(any(JobCard.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transitDamageReportRepository.save(any(TransitDamageReport.class))).thenAnswer(inv -> inv.getArgument(0));

        transitService.approveReport(reportId, "admin");

        assertEquals(TransitDamageStatus.APPROVED, report.getStatus());
        assertEquals(WorkOrderStatus.IN_PROGRESS, wo.getStatus());
        verify(workOrderRepository, times(1)).save(wo);
    }
}
