package com.kalibyte.YashTools.sales.invoice.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceItemRequest;
import com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceRequest;
import com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceResponse;
import com.kalibyte.YashTools.sales.invoice.entity.SalesInvoice;
import com.kalibyte.YashTools.sales.invoice.entity.SalesInvoiceItem;
import com.kalibyte.YashTools.sales.invoice.repository.SalesInvoiceRepository;
import com.kalibyte.YashTools.sales.invoice.service.SalesInvoiceService;
import com.kalibyte.YashTools.workorder.entity.WorkOrder;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import com.kalibyte.YashTools.workorder.repository.WorkOrderItemRepository;
import com.kalibyte.YashTools.workorder.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.kalibyte.YashTools.sales.invoice.repository.SalesInvoiceItemRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesInvoiceServiceImpl implements SalesInvoiceService {

    private final SalesInvoiceRepository repository;
    private final SalesInvoiceItemRepository salesInvoiceItemRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderItemRepository workOrderItemRepository;

    @Override
    @Transactional
    public SalesInvoiceResponse createInvoice(SalesInvoiceRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        String companyCode = CompanyContextHolder.getCompanyCode();

        WorkOrder wo = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new BusinessException("Work Order not found with ID: " + request.getWorkOrderId()));

        if (!wo.getCompany().getId().equals(companyId)) {
            throw new BusinessException("Work Order does not belong to the active company context");
        }

        if (!wo.getItems().isEmpty()) {
            boolean allFullyInvoiced = wo.getItems().stream().allMatch(item -> {
                int totalQty = item.getQuantity() != null ? item.getQuantity() : 0;
                int alreadyInvoiced = salesInvoiceItemRepository.getSumQuantityByWorkOrderItemId(item.getId());
                return alreadyInvoiced >= totalQty;
            });
            if (allFullyInvoiced) {
                throw new BusinessException(String.format("Work Order '%s' is already fully invoiced (100%% billed). No further invoices can be created.", wo.getWorkOrderNo()));
            }
        }

        long count = repository.count() + 1;
        String invoiceNo = String.format("%s-INV-%d-%06d", companyCode, LocalDate.now().getYear(), count);

        BigDecimal subTotal = BigDecimal.ZERO;
        List<SalesInvoiceItem> items = new ArrayList<>();

        for (SalesInvoiceItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw new BusinessException("Sales Invoice item quantity must be greater than zero");
            }

            WorkOrderItem woi = workOrderItemRepository.findById(itemReq.getWorkOrderItemId())
                    .orElseThrow(() -> new BusinessException("Work Order Item not found with ID: " + itemReq.getWorkOrderItemId()));

            if (!woi.getWorkOrder().getId().equals(wo.getId())) {
                throw new BusinessException("Item " + woi.getId() + " does not belong to Work Order " + wo.getId());
            }

            if (Boolean.TRUE.equals(woi.getTrial())) {
                if (woi.getTrialStatus() == null || woi.getTrialStatus() == com.kalibyte.YashTools.workorder.entity.enums.TrialStatus.PENDING) {
                    String itemName = (woi.getToolName() != null && !woi.getToolName().isBlank()) ? woi.getToolName() : woi.getItemName();
                    throw new BusinessException(String.format("Cannot generate bill for trial item '%s' because its trial status is still PENDING. The trial must be marked as SUCCESS first.", itemName));
                }
                if (woi.getTrialStatus() == com.kalibyte.YashTools.workorder.entity.enums.TrialStatus.FAILED) {
                    String itemName = (woi.getToolName() != null && !woi.getToolName().isBlank()) ? woi.getToolName() : woi.getItemName();
                    throw new BusinessException(String.format("Cannot generate bill for trial item '%s' because the trial has FAILED.", itemName));
                }
            }

            int alreadyInvoicedQty = salesInvoiceItemRepository.getSumQuantityByWorkOrderItemId(woi.getId());
            int totalItemQty = woi.getQuantity() != null ? woi.getQuantity() : 0;
            int remainingQty = Math.max(0, totalItemQty - alreadyInvoicedQty);

            if (remainingQty <= 0) {
                String itemName = (woi.getToolName() != null && !woi.getToolName().isBlank()) ? woi.getToolName() : woi.getItemName();
                throw new BusinessException(String.format("Item '%s' in Work Order '%s' is already 100%% invoiced (Total ordered: %d, Already invoiced: %d). No further quantity can be billed for this item.", itemName, wo.getWorkOrderNo(), totalItemQty, alreadyInvoicedQty));
            }

            if (itemReq.getQuantity() > remainingQty) {
                String itemName = (woi.getToolName() != null && !woi.getToolName().isBlank()) ? woi.getToolName() : woi.getItemName();
                throw new BusinessException(String.format("Requested quantity (%d) for invoice item '%s' exceeds remaining un-invoiced quantity (%d). Total ordered: %d, Already invoiced: %d",
                        itemReq.getQuantity(), itemName, remainingQty, totalItemQty, alreadyInvoicedQty));
            }

            if (woi.getQuotationItem() == null || woi.getQuotationItem().getUnitPrice() == null) {
                String itemName = (woi.getToolName() != null && !woi.getToolName().isBlank()) ? woi.getToolName() : woi.getItemName();
                throw new BusinessException(String.format("Cannot create invoice item for '%s': Unit price is not available on the associated quotation item.", itemName));
            }

            BigDecimal unitPrice = woi.getQuotationItem().getUnitPrice();
            BigDecimal qtyBD = BigDecimal.valueOf(itemReq.getQuantity());
            BigDecimal totalPrice = unitPrice.multiply(qtyBD).setScale(2, RoundingMode.HALF_UP);
            subTotal = subTotal.add(totalPrice);

            SalesInvoiceItem sii = SalesInvoiceItem.builder()
                    .workOrderItem(woi)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .build();
            items.add(sii);
        }

        BigDecimal cgstRate = BigDecimal.ZERO;
        BigDecimal cgstAmount = BigDecimal.ZERO;
        BigDecimal sgstRate = BigDecimal.ZERO;
        BigDecimal sgstAmount = BigDecimal.ZERO;
        BigDecimal igstRate = BigDecimal.ZERO;
        BigDecimal igstAmount = BigDecimal.ZERO;

        if (request.getIsInterstate()) {
            igstRate = BigDecimal.valueOf(18.00);
            igstAmount = subTotal.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        } else {
            cgstRate = BigDecimal.valueOf(9.00);
            cgstAmount = subTotal.multiply(BigDecimal.valueOf(0.09)).setScale(2, RoundingMode.HALF_UP);
            sgstRate = BigDecimal.valueOf(9.00);
            sgstAmount = subTotal.multiply(BigDecimal.valueOf(0.09)).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAmount = subTotal.add(cgstAmount).add(sgstAmount).add(igstAmount).setScale(2, RoundingMode.HALF_UP);

        SalesInvoice invoice = SalesInvoice.builder()
                .invoiceNo(invoiceNo)
                .workOrder(wo)
                .invoiceDate(LocalDate.now())
                .subTotal(subTotal)
                .cgstRate(cgstRate)
                .cgstAmount(cgstAmount)
                .sgstRate(sgstRate)
                .sgstAmount(sgstAmount)
                .igstRate(igstRate)
                .igstAmount(igstAmount)
                .totalAmount(totalAmount)
                .status("DRAFT")
                .remarks(request.getRemarks())
                .items(new ArrayList<>())
                .build();
        invoice.setCompany(wo.getCompany());

        for (SalesInvoiceItem item : items) {
            invoice.addItem(item);
        }

        SalesInvoice saved = repository.save(invoice);
        log.info("Created Sales Invoice {} for Work Order {}", invoiceNo, wo.getWorkOrderNo());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesInvoiceResponse getInvoiceById(UUID id) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        SalesInvoice invoice = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Sales Invoice not found with ID: " + id));
        return toResponse(invoice);
    }

    @Override
    @Transactional
    public SalesInvoiceResponse recordPayment(UUID id) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        SalesInvoice invoice = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Sales Invoice not found with ID: " + id));

        if (!"DRAFT".equals(invoice.getStatus())) {
            throw new BusinessException("Only DRAFT invoices can be paid");
        }

        invoice.setStatus("PAID");
        SalesInvoice saved = repository.save(invoice);
        log.info("Recorded payment for Sales Invoice {}", invoice.getInvoiceNo());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SalesInvoiceResponse> getAllInvoices(int page, int size) {
        Page<SalesInvoice> paged = repository.findAll(PageRequest.of(page, size));
        return PageResponse.from(paged, this::toResponse);
    }

    private SalesInvoiceResponse toResponse(SalesInvoice inv) {
        WorkOrder wo = inv.getWorkOrder();
        return SalesInvoiceResponse.builder()
                .id(inv.getId())
                .invoiceNo(inv.getInvoiceNo())
                .workOrderId(wo != null ? wo.getId() : null)
                .workOrderNo(wo != null ? wo.getWorkOrderNo() : null)
                .customerName(wo != null ? wo.getCustomerCompanyName() : null)
                .customerContactPerson(wo != null ? wo.getCustomerContactPerson() : null)
                .customerEmail(wo != null ? wo.getCustomerEmail() : null)
                .customerMobile(wo != null ? wo.getCustomerMobile() : null)
                .shippingAddress(wo != null ? (wo.getShippingAddress() != null ? wo.getShippingAddress() : (wo.getCustomer() != null ? wo.getCustomer().getDeliveryAddress() : null)) : null)
                .poNumber(wo != null ? wo.getPoNumber() : null)
                .quotationId(wo != null && wo.getQuotation() != null ? wo.getQuotation().getId() : null)
                .quotationNo(wo != null && wo.getQuotation() != null ? wo.getQuotation().getQuotationNo() : null)
                .invoiceDate(inv.getInvoiceDate())
                .subTotal(inv.getSubTotal() != null ? inv.getSubTotal().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .cgstRate(inv.getCgstRate())
                .cgstAmount(inv.getCgstAmount() != null ? inv.getCgstAmount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .sgstRate(inv.getSgstRate())
                .sgstAmount(inv.getSgstAmount() != null ? inv.getSgstAmount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .igstRate(inv.getIgstRate())
                .igstAmount(inv.getIgstAmount() != null ? inv.getIgstAmount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .totalAmount(inv.getTotalAmount() != null ? inv.getTotalAmount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .status(inv.getStatus())
                .remarks(inv.getRemarks())
                .companyId(inv.getCompany() != null ? inv.getCompany().getId() : null)
                .companyCode(inv.getCompany() != null ? inv.getCompany().getCode() : null)
                .items(inv.getItems().stream()
                        .map(i -> {
                            WorkOrderItem woi = i.getWorkOrderItem();
                            int totalItemQty = woi.getQuantity() != null ? woi.getQuantity() : 0;
                            int alreadyInvoiced = salesInvoiceItemRepository.getSumQuantityByWorkOrderItemId(woi.getId());
                            int remaining = Math.max(0, totalItemQty - alreadyInvoiced);

                            return SalesInvoiceResponse.ItemResponse.builder()
                                    .id(i.getId())
                                    .workOrderItemId(woi.getId())
                                    .toolName(woi.getToolName())
                                    .itemName(woi.getItemName())
                                    .quantity(i.getQuantity())
                                    .orderedQuantity(totalItemQty)
                                    .alreadyInvoicedQuantity(alreadyInvoiced)
                                    .remainingUninvoicedQuantity(remaining)
                                    .unitPrice(i.getUnitPrice() != null ? i.getUnitPrice().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                                    .totalPrice(i.getTotalPrice() != null ? i.getTotalPrice().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                                    .diameter(woi.getDiameter())
                                    .shankDiameter(woi.getShankDiameter())
                                    .overallLength(woi.getOverallLength())
                                    .fluteLength(woi.getFluteLength())
                                    .drawingReference(woi.getDrawingReference())
                                    .materialGrade(woi.getMaterialGrade() != null ? woi.getMaterialGrade().name() : null)
                                    .materialType(woi.getMaterialType())
                                    .coatingType(woi.getCoatingType())
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<SalesInvoiceResponse> searchInvoices(String query, int page, int size) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<SalesInvoice> result = repository.searchInvoices(companyId, query, pageable);
        return PageResponse.from(result, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceDashboardSummaryResponse getDashboardSummary() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        List<SalesInvoice> invoices = repository.findByCompanyId(companyId);

        BigDecimal grossInvoiced = BigDecimal.ZERO;
        BigDecimal collectionsSettled = BigDecimal.ZERO;
        long paidCount = 0;
        long pendingCount = 0;

        for (SalesInvoice inv : invoices) {
            BigDecimal amt = inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO;
            grossInvoiced = grossInvoiced.add(amt);
            if ("PAID".equalsIgnoreCase(inv.getStatus())) {
                collectionsSettled = collectionsSettled.add(amt);
                paidCount++;
            } else if (!"CANCELLED".equalsIgnoreCase(inv.getStatus())) {
                pendingCount++;
            }
        }

        BigDecimal outstanding = grossInvoiced.subtract(collectionsSettled).max(BigDecimal.ZERO);

        return com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceDashboardSummaryResponse.builder()
                .grossSalesInvoiced(grossInvoiced.setScale(2, RoundingMode.HALF_UP))
                .totalInvoicesCount(invoices.size())
                .collectionsSettled(collectionsSettled.setScale(2, RoundingMode.HALF_UP))
                .paidInvoicesCount(paidCount)
                .outstandingReceivables(outstanding.setScale(2, RoundingMode.HALF_UP))
                .pendingInvoicesCount(pendingCount)
                .build();
    }
}
