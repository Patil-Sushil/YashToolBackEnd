package com.kalibyte.YashTools.production.logistics.damage.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.production.jobcard.entity.JobCard;
import com.kalibyte.YashTools.production.jobcard.entity.enums.JobCardStatus;
import com.kalibyte.YashTools.production.jobcard.repository.JobCardRepository;
import com.kalibyte.YashTools.production.logistics.damage.dto.CreateTransitDamageRequest;
import com.kalibyte.YashTools.production.logistics.damage.dto.TransitDamageResponse;
import com.kalibyte.YashTools.production.logistics.damage.entity.TransitDamageReport;
import com.kalibyte.YashTools.production.logistics.damage.entity.enums.TransitDamageAction;
import com.kalibyte.YashTools.production.logistics.damage.entity.enums.TransitDamageStatus;
import com.kalibyte.YashTools.production.logistics.damage.repository.TransitDamageReportRepository;
import com.kalibyte.YashTools.production.logistics.damage.service.TransitDamageService;
import com.kalibyte.YashTools.production.logistics.entity.DeliveryChallan;
import com.kalibyte.YashTools.production.logistics.entity.DeliveryChallanItem;
import com.kalibyte.YashTools.production.logistics.repository.DeliveryChallanRepository;
import com.kalibyte.YashTools.workorder.entity.WorkOrder;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import com.kalibyte.YashTools.workorder.entity.enums.WorkOrderStatus;
import com.kalibyte.YashTools.workorder.repository.WorkOrderItemRepository;
import com.kalibyte.YashTools.workorder.repository.WorkOrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransitDamageServiceImpl implements TransitDamageService {

    private final TransitDamageReportRepository transitDamageReportRepository;
    private final DeliveryChallanRepository deliveryChallanRepository;
    private final WorkOrderItemRepository workOrderItemRepository;
    private final JobCardRepository jobCardRepository;
    private final WorkOrderRepository workOrderRepository;

    @Override
    @Transactional
    public TransitDamageResponse reportTransitDamage(CreateTransitDamageRequest request, String username) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        String companyCode = CompanyContextHolder.getCompanyCode();

        DeliveryChallan challan = deliveryChallanRepository.findByIdAndCompanyId(request.getDeliveryChallanId(), companyId)
                .orElseThrow(() -> new BusinessException("Delivery Challan not found with ID: " + request.getDeliveryChallanId()));

        if (!"DISPATCHED".equals(challan.getStatus())) {
            throw new BusinessException("Transit damage can only be reported while the delivery challan is in DISPATCHED status (in transit)");
        }

        WorkOrderItem woi = workOrderItemRepository.findById(request.getWorkOrderItemId())
                .orElseThrow(() -> new BusinessException("Work Order Item not found with ID: " + request.getWorkOrderItemId()));

        // Validate item is part of the challan
        DeliveryChallanItem challanItem = challan.getItems().stream()
                .filter(item -> item.getWorkOrderItem().getId().equals(woi.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Work Order Item is not part of this Delivery Challan"));

        if (request.getDamagedQuantity() > challanItem.getQuantity()) {
            throw new BusinessException("Damaged quantity (" + request.getDamagedQuantity() + 
                    ") cannot exceed shipped quantity (" + challanItem.getQuantity() + ")");
        }

        TransitDamageReport report = TransitDamageReport.builder()
                .deliveryChallan(challan)
                .workOrderItem(woi)
                .damagedQuantity(request.getDamagedQuantity())
                .action(request.getAction())
                .status(TransitDamageStatus.REPORTED)
                .reportedBy(username)
                .reportedAt(LocalDateTime.now())
                .remarks(request.getRemarks())
                .build();
        report.setCompany(Company.builder().id(companyId).code(companyCode).build());

        TransitDamageReport saved = transitDamageReportRepository.save(report);
        log.info("Transit damage reported for Challan {}, Item {}. Quantity: {}", challan.getChallanNo(), woi.getId(), request.getDamagedQuantity());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public TransitDamageResponse approveReport(UUID reportId, String username) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        String companyCode = CompanyContextHolder.getCompanyCode();

        TransitDamageReport report = transitDamageReportRepository.findByIdAndCompanyId(reportId, companyId)
                .orElseThrow(() -> new BusinessException("Transit Damage Report not found with ID: " + reportId));

        if (report.getStatus() != TransitDamageStatus.REPORTED) {
            throw new BusinessException("Only REPORTED transit damage claims can be approved. Current status: " + report.getStatus());
        }

        report.setStatus(TransitDamageStatus.APPROVED);
        report.setUpdatedBy(username);
        report.setUpdatedAt(LocalDateTime.now());

        WorkOrderItem woi = report.getWorkOrderItem();
        DeliveryChallan challan = report.getDeliveryChallan();

        if (report.getAction() == TransitDamageAction.REWORK || report.getAction() == TransitDamageAction.REPLACE) {
            // Generate a Rework/Replacement Job Card
            long seq = jobCardRepository.count() + 1;
            String jobCardNo = String.format("%s-JC-%d-%06d", companyCode, LocalDate.now().getYear(), seq);

            JobCard jc = JobCard.builder()
                    .jobCardNo(jobCardNo)
                    .workOrder(woi.getWorkOrder())
                    .workOrderItem(woi)
                    .status(JobCardStatus.CREATED)
                    .priority(1)
                    .totalQuantity(report.getDamagedQuantity())
                    .remarks("AUTO-GENERATED: Damaged during transit under Challan " + 
                            challan.getChallanNo() + ". Action: " + report.getAction() + ". Details: " + report.getRemarks())
                    .isRework(report.getAction() == TransitDamageAction.REWORK)
                    .build();
            jc.setCompany(Company.builder().id(companyId).code(companyCode).build());

            if (report.getAction() == TransitDamageAction.REWORK) {
                List<JobCard> parentJcs = jobCardRepository.findByWorkOrderItemIdAndCompanyId(woi.getId(), companyId);
                if (!parentJcs.isEmpty()) {
                    jc.setReworkParentJobCard(parentJcs.get(0));
                }
            }

            JobCard savedJc = jobCardRepository.save(jc);
            report.setCreatedJobCard(savedJc);

            // Re-open Work Order by setting status back to IN_PROGRESS since rework/replacement is required
            WorkOrder wo = woi.getWorkOrder();
            if (wo.getStatus() == WorkOrderStatus.COMPLETED || wo.getStatus() == WorkOrderStatus.PRODUCTION_COMPLETED) {
                wo.setStatus(WorkOrderStatus.IN_PROGRESS);
                workOrderRepository.save(wo);
                log.info("Work Order {} reopened to IN_PROGRESS due to transit damage", wo.getWorkOrderNo());
            }
        }

        TransitDamageReport saved = transitDamageReportRepository.save(report);
        log.info("Transit damage report {} approved by {}", reportId, username);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public TransitDamageResponse rejectReport(UUID reportId, String username) {
        UUID companyId = CompanyContextHolder.getCompanyId();

        TransitDamageReport report = transitDamageReportRepository.findByIdAndCompanyId(reportId, companyId)
                .orElseThrow(() -> new BusinessException("Transit Damage Report not found with ID: " + reportId));

        if (report.getStatus() != TransitDamageStatus.REPORTED) {
            throw new BusinessException("Only REPORTED transit damage claims can be rejected. Current status: " + report.getStatus());
        }

        report.setStatus(TransitDamageStatus.REJECTED);
        report.setUpdatedBy(username);
        report.setUpdatedAt(LocalDateTime.now());

        TransitDamageReport saved = transitDamageReportRepository.save(report);
        log.info("Transit damage report {} rejected by {}", reportId, username);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransitDamageResponse> getDamageReportsForChallan(UUID challanId) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        return transitDamageReportRepository.findByDeliveryChallanIdAndCompanyId(challanId, companyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TransitDamageResponse getReportById(UUID id) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        TransitDamageReport report = transitDamageReportRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Transit Damage Report not found with ID: " + id));
        return toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransitDamageResponse> getAllReports() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        return transitDamageReportRepository.findByCompanyId(companyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TransitDamageResponse toResponse(TransitDamageReport report) {
        return TransitDamageResponse.builder()
                .id(report.getId())
                .deliveryChallanId(report.getDeliveryChallan().getId())
                .deliveryChallanNo(report.getDeliveryChallan().getChallanNo())
                .workOrderItemId(report.getWorkOrderItem().getId())
                .toolName(report.getWorkOrderItem().getToolName())
                .itemName(report.getWorkOrderItem().getItemName())
                .damagedQuantity(report.getDamagedQuantity())
                .action(report.getAction().name())
                .status(report.getStatus().name())
                .reportedBy(report.getReportedBy())
                .reportedAt(report.getReportedAt())
                .remarks(report.getRemarks())
                .createdJobCardId(report.getCreatedJobCard() != null ? report.getCreatedJobCard().getId() : null)
                .createdJobCardNo(report.getCreatedJobCard() != null ? report.getCreatedJobCard().getJobCardNo() : null)
                .build();
    }
}
