package com.kalibyte.YashTools.dashboard.service.impl;

import com.kalibyte.YashTools.audit.entity.AuditLog;
import com.kalibyte.YashTools.audit.repository.AuditLogRepository;
import com.kalibyte.YashTools.auth.entity.User;
import com.kalibyte.YashTools.auth.repository.UserRepository;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.repository.CompanyRepository;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.dashboard.dto.*;
import com.kalibyte.YashTools.dashboard.service.DashboardService;
import com.kalibyte.YashTools.enquiry.entity.enums.EnquiryStatus;
import com.kalibyte.YashTools.enquiry.repository.EnquiryRepository;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.inventory.transaction.stock.entity.Stock;
import com.kalibyte.YashTools.inventory.transaction.stock.repository.StockRepository;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.repository.StockTransactionRepository;
import com.kalibyte.YashTools.labors.attendance.repository.AttendanceRepository;
import com.kalibyte.YashTools.labors.labor.repository.LaborerRepository;
import com.kalibyte.YashTools.production.execution.entity.ExecutionLog;
import com.kalibyte.YashTools.production.execution.repository.ExecutionLogRepository;
import com.kalibyte.YashTools.production.jobcard.entity.enums.JobCardStatus;
import com.kalibyte.YashTools.production.jobcard.repository.JobCardRepository;
import com.kalibyte.YashTools.production.machine.entity.Machine;
import com.kalibyte.YashTools.production.machine.entity.enums.MachineStatus;
import com.kalibyte.YashTools.production.machine.repository.MachineRepository;
import com.kalibyte.YashTools.production.planning.entity.enums.ScheduleStatus;
import com.kalibyte.YashTools.production.planning.repository.ProductionScheduleRepository;
import com.kalibyte.YashTools.purchase.shared.enums.PurchaseOrderStatus;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.repository.PurchaseInvoiceRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrderItem;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.repository.PurchaseOrderItemRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.YashTools.quotation.entity.enums.QuotationStatus;
import com.kalibyte.YashTools.quotation.repository.QuotationRepository;
import com.kalibyte.YashTools.sales.invoice.entity.SalesInvoice;
import com.kalibyte.YashTools.sales.invoice.repository.SalesInvoiceRepository;
import com.kalibyte.YashTools.workorder.entity.enums.WorkOrderStatus;
import com.kalibyte.YashTools.workorder.repository.WorkOrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final EnquiryRepository enquiryRepository;
    private final QuotationRepository quotationRepository;
    private final WorkOrderRepository workOrderRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final AuditLogRepository auditLogRepository;
    private final AttendanceRepository attendanceRepository;
    private final LaborerRepository laborerRepository;
    private final JobCardRepository jobCardRepository;
    private final MachineRepository machineRepository;
    private final ExecutionLogRepository executionLogRepository;
    private final ProductionScheduleRepository scheduleRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final ItemRepository itemRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        String companyCode = CompanyContextHolder.getCompanyCode();

        long totalUsers = userRepository.countByCompanyCode(companyCode);
        long activeUsers = userRepository.countByCompanyCodeAndEnabledTrue(companyCode);
        long totalEnquiries = enquiryRepository.count();
        long totalQuotations = quotationRepository.count();
        long totalWorkOrders = workOrderRepository.count();

        BigDecimal revenue = salesInvoiceRepository.getTotalRevenue();
        BigDecimal totalInvoicedRevenue = (revenue != null) ? revenue : BigDecimal.ZERO;

        Company company = companyRepository.findById(companyId).orElse(null);
        Map<String, Object> details = new HashMap<>();
        if (company != null) {
            details.put("id", company.getId());
            details.put("code", company.getCode());
            details.put("name", company.getName());
            details.put("gstNumber", company.getGstNumber());
            details.put("email", company.getEmail());
            details.put("phone", company.getPhone());
        }

        List<UUID> userIds = userRepository.findAll().stream()
                .filter(u -> companyCode.equalsIgnoreCase(u.getCompanyCode()))
                .map(User::getId)
                .collect(Collectors.toList());

        List<AuditLogDto> auditLogDtos = new ArrayList<>();
        if (!userIds.isEmpty()) {
            Page<AuditLog> page = auditLogRepository.findAll((root, query, cb) -> root.get("userId").in(userIds),
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp")));
            auditLogDtos = page.getContent().stream()
                    .map(log -> AuditLogDto.builder()
                            .username(log.getUsername())
                            .action(log.getAction() != null ? log.getAction().name() : null)
                            .actionDescription(log.getActionDescription())
                            .entityType(log.getEntityType())
                            .status(log.getStatus() != null ? log.getStatus().name() : null)
                            .timestamp(log.getTimestamp())
                            .build())
                    .collect(Collectors.toList());
        }

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalEnquiries(totalEnquiries)
                .totalQuotations(totalQuotations)
                .totalWorkOrders(totalWorkOrders)
                .totalInvoicedRevenue(totalInvoicedRevenue)
                .companyDetails(details)
                .recentAuditLogs(auditLogDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SupervisorDashboardResponse getSupervisorDashboard() {
        UUID companyId = CompanyContextHolder.getCompanyId();

        long activeLaborers = attendanceRepository.countByWorkDate(LocalDate.now());
        long totalLaborers = laborerRepository.countByIsActiveTrue();

        long runningJobCards = jobCardRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("company").get("id"), companyId),
                root.get("status").in(JobCardStatus.STARTED, JobCardStatus.ASSIGNED)
        ));

        long pendingJobCards = jobCardRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("company").get("id"), companyId),
                root.get("status").in(JobCardStatus.CREATED, JobCardStatus.PLANNED)
        ));

        long completedToday = jobCardRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("company").get("id"), companyId),
                cb.equal(root.get("status"), JobCardStatus.COMPLETED),
                cb.greaterThanOrEqualTo(root.get("updatedAt"), LocalDate.now().atStartOfDay())
        ));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<ExecutionLog> todayLogs = executionLogRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("jobCard").get("company").get("id"), companyId),
                cb.greaterThanOrEqualTo(root.get("startTime"), startOfDay)
        ));

        int todayProduced = todayLogs.stream().mapToInt(ExecutionLog::getProducedQuantity).sum();
        int todayRejected = todayLogs.stream().mapToInt(ExecutionLog::getRejectedQuantity).sum();

        List<Machine> machines = machineRepository.findAll((root, query, cb) ->
                cb.equal(root.get("company").get("id"), companyId)
        );

        Map<String, Long> machineStatus = machines.stream()
                .collect(Collectors.groupingBy(m -> m.getStatus().name(), Collectors.counting()));

        return SupervisorDashboardResponse.builder()
                .activeLaborersCount(activeLaborers)
                .totalLaborers(totalLaborers)
                .runningJobCards(runningJobCards)
                .pendingJobCards(pendingJobCards)
                .completedJobCardsToday(completedToday)
                .todayProducedQuantity(todayProduced)
                .todayRejectedQuantity(todayRejected)
                .machineStatusCounts(machineStatus)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ManagerDashboardResponse getManagerDashboard() {
        UUID companyId = CompanyContextHolder.getCompanyId();

        long pendingQuotationApprovals = quotationRepository.countByStatus(QuotationStatus.PENDING_APPROVAL);
        long workOrdersCompleted = workOrderRepository.countByStatus(WorkOrderStatus.COMPLETED);
        long workOrdersPending = workOrderRepository.count((root, query, cb) -> root.get("status").in(
                WorkOrderStatus.CREATED, WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.PRODUCTION_COMPLETED
        ));

        BigDecimal expense = purchaseInvoiceRepository.getTotalExpense(companyId);
        BigDecimal totalPurchaseInvoiceExpense = (expense != null) ? expense : BigDecimal.ZERO;

        BigDecimal revenue = salesInvoiceRepository.getTotalRevenue();
        BigDecimal totalSalesInvoiceRevenue = (revenue != null) ? revenue : BigDecimal.ZERO;

        long delayedJobs = scheduleRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("company").get("id"), companyId),
                cb.lessThan(root.get("plannedEndDate"), LocalDate.now()),
                cb.notEqual(root.get("status"), ScheduleStatus.COMPLETED)
        ));

        List<Machine> machines = machineRepository.findAll((root, query, cb) ->
                cb.equal(root.get("company").get("id"), companyId)
        );

        long activeCount = machines.stream().filter(m -> m.getStatus() == MachineStatus.ACTIVE).count();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<ExecutionLog> todayLogs = executionLogRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("jobCard").get("company").get("id"), companyId),
                cb.greaterThanOrEqualTo(root.get("startTime"), startOfDay)
        ));

        long runningMachinesCount = todayLogs.stream()
                .filter(l -> l.getEndTime() == null)
                .map(l -> l.getMachine().getId())
                .distinct()
                .count();

        double machineUtilization = activeCount > 0 ? (double) runningMachinesCount * 100.0 / activeCount : 0.0;

        return ManagerDashboardResponse.builder()
                .pendingQuotationApprovals(pendingQuotationApprovals)
                .workOrdersCompleted(workOrdersCompleted)
                .workOrdersPending(workOrdersPending)
                .totalPurchaseInvoiceExpense(totalPurchaseInvoiceExpense)
                .totalSalesInvoiceRevenue(totalSalesInvoiceRevenue)
                .delayedJobsCount(delayedJobs)
                .averageMachineUtilization(machineUtilization)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesDashboardResponse getSalesDashboard() {
        long totalEnquiries = enquiryRepository.count();
        long openEnquiries = enquiryRepository.count((root, query, cb) -> root.get("status").in(
                EnquiryStatus.CREATED, EnquiryStatus.UNDER_REVIEW, EnquiryStatus.QUOTED
        ));
        long closedEnquiries = enquiryRepository.count((root, query, cb) -> root.get("status").in(
                EnquiryStatus.ACCEPTED, EnquiryStatus.CLOSED
        ));

        long totalQuotations = quotationRepository.count();
        long rootQuotations = quotationRepository.countByParentQuotationIsNull();
        long revisedQuotations = quotationRepository.countByParentQuotationIsNotNull();
        long approvedQuotations = quotationRepository.countByStatus(QuotationStatus.ADMIN_APPROVED)
                + quotationRepository.countByStatus(QuotationStatus.APPROVED)
                + quotationRepository.countByStatus(QuotationStatus.CUSTOMER_APPROVED);
        long pendingApprovalQuotations = quotationRepository.countByStatus(QuotationStatus.PENDING_APPROVAL);

        double conversionRate = totalQuotations > 0 ? (double) approvedQuotations * 100.0 / totalQuotations : 0.0;

        BigDecimal revenue = salesInvoiceRepository.getTotalRevenue();
        BigDecimal salesInvoiceRevenue = (revenue != null) ? revenue : BigDecimal.ZERO;

        List<SalesInvoice> allInvoices = salesInvoiceRepository.findAll();
        Map<String, List<SalesInvoice>> invoicesByCustomer = allInvoices.stream()
                .filter(si -> si.getWorkOrder() != null && si.getWorkOrder().getCustomer() != null)
                .collect(Collectors.groupingBy(si -> si.getWorkOrder().getCustomer().getCustomerName()));

        List<CustomerSalesDto> topCustomers = invoicesByCustomer.entrySet().stream()
                .map(entry -> {
                    BigDecimal totalSales = entry.getValue().stream()
                            .map(SalesInvoice::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return CustomerSalesDto.builder()
                            .customerName(entry.getKey())
                            .totalSales(totalSales)
                            .invoiceCount(entry.getValue().size())
                            .build();
                })
                .sorted((c1, c2) -> c2.getTotalSales().compareTo(c1.getTotalSales()))
                .limit(5)
                .collect(Collectors.toList());

        return SalesDashboardResponse.builder()
                .totalEnquiries(totalEnquiries)
                .openEnquiries(openEnquiries)
                .closedEnquiries(closedEnquiries)
                .totalQuotations(totalQuotations)
                .rootQuotations(rootQuotations)
                .revisedQuotations(revisedQuotations)
                .approvedQuotations(approvedQuotations)
                .pendingApprovalQuotations(pendingApprovalQuotations)
                .quotationConversionRate(conversionRate)
                .salesInvoiceRevenue(salesInvoiceRevenue)
                .topCustomers(topCustomers)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionDashboardResponse getProductionDashboard() {
        UUID companyId = CompanyContextHolder.getCompanyId();

        long runningJobs = jobCardRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("company").get("id"), companyId),
                root.get("status").in(JobCardStatus.STARTED, JobCardStatus.ASSIGNED)
        ));

        long completedJobs = jobCardRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("company").get("id"), companyId),
                cb.equal(root.get("status"), JobCardStatus.COMPLETED)
        ));

        long pendingJobs = jobCardRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("company").get("id"), companyId),
                root.get("status").in(JobCardStatus.CREATED, JobCardStatus.PLANNED)
        ));

        long delayedJobs = scheduleRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("company").get("id"), companyId),
                cb.lessThan(root.get("plannedEndDate"), LocalDate.now()),
                cb.notEqual(root.get("status"), ScheduleStatus.COMPLETED)
        ));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<ExecutionLog> todayLogs = executionLogRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("jobCard").get("company").get("id"), companyId),
                cb.greaterThanOrEqualTo(root.get("startTime"), startOfDay)
        ));

        int todayProduced = todayLogs.stream().mapToInt(ExecutionLog::getProducedQuantity).sum();
        int todayRejected = todayLogs.stream().mapToInt(ExecutionLog::getRejectedQuantity).sum();

        List<Machine> machines = machineRepository.findAll((root, query, cb) ->
                cb.equal(root.get("company").get("id"), companyId)
        );

        Map<String, Long> machineStatus = machines.stream()
                .collect(Collectors.groupingBy(m -> m.getStatus().name(), Collectors.counting()));

        long activeCount = machines.stream().filter(m -> m.getStatus() == MachineStatus.ACTIVE).count();
        long runningMachinesCount = todayLogs.stream()
                .filter(l -> l.getEndTime() == null)
                .map(l -> l.getMachine().getId())
                .distinct()
                .count();

        double machineUtilization = activeCount > 0 ? (double) runningMachinesCount * 100.0 / activeCount : 0.0;

        return ProductionDashboardResponse.builder()
                .runningJobsCount(runningJobs)
                .completedJobsCount(completedJobs)
                .delayedJobsCount(delayedJobs)
                .pendingJobsCount(pendingJobs)
                .todayProducedQuantity(todayProduced)
                .todayRejectedQuantity(todayRejected)
                .machineUtilizationPercentage(machineUtilization)
                .machineStatusCounts(machineStatus)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StoreDashboardResponse getStoreDashboard() {
        UUID companyId = CompanyContextHolder.getCompanyId();

        long totalItems = itemRepository.count();

        List<Stock> allStocks = stockRepository.findAll();
        long lowStockCount = allStocks.stream()
                .filter(s -> s.getQuantity() != null && s.getQuantity().compareTo(new BigDecimal("10.0")) < 0)
                .count();

        long pendingGoodsReceipts = purchaseOrderRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("company").get("id"), companyId),
                root.get("status").in(PurchaseOrderStatus.APPROVED, PurchaseOrderStatus.PARTIALLY_RECEIVED)
        ));

        List<PurchaseOrderItem> allPoItems = purchaseOrderItemRepository.findAll();
        Map<UUID, BigDecimal> latestRateByItem = allPoItems.stream()
                .filter(poItem -> poItem.getItem() != null && poItem.getItem().getId() != null)
                .collect(Collectors.toMap(
                        poItem -> poItem.getItem().getId(),
                        poItem -> poItem.getRate() != null ? poItem.getRate() : BigDecimal.ZERO,
                        (rate1, rate2) -> rate2 // keep latest
                ));

        BigDecimal totalStockValue = allStocks.stream()
                .filter(stock -> stock.getItem() != null && stock.getQuantity() != null)
                .map(stock -> {
                    BigDecimal rate = latestRateByItem.getOrDefault(stock.getItem().getId(), new BigDecimal("100.00"));
                    return stock.getQuantity().multiply(rate);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<StockTransactionDto> recentTransactions = stockTransactionRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent().stream()
                .map(tx -> StockTransactionDto.builder()
                        .transactionType(tx.getTransactionType() != null ? tx.getTransactionType().name() : null)
                        .itemName(tx.getItem() != null ? tx.getItem().getName() : null)
                        .itemSku(tx.getItem() != null ? tx.getItem().getSku() : null)
                        .quantity(tx.getQuantity())
                        .referenceNumber(tx.getReferenceNumber())
                        .timestamp(tx.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return StoreDashboardResponse.builder()
                .totalItemsCount(totalItems)
                .lowStockItemsCount(lowStockCount)
                .pendingGoodsReceipts(pendingGoodsReceipts)
                .totalStockValue(totalStockValue)
                .recentStockTransactions(recentTransactions)
                .build();
    }
}
