package com.kalibyte.YashTools.production.jobcard.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.production.jobcard.dto.CreateJobCardRequest;
import com.kalibyte.YashTools.production.jobcard.dto.JobCardResponse;
import com.kalibyte.YashTools.production.jobcard.dto.SplitJobCardRequest;
import com.kalibyte.YashTools.production.jobcard.entity.JobCard;
import com.kalibyte.YashTools.production.jobcard.entity.enums.JobCardStatus;
import com.kalibyte.YashTools.production.jobcard.repository.JobCardRepository;
import com.kalibyte.YashTools.production.jobcard.service.JobCardService;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import com.kalibyte.YashTools.workorder.repository.WorkOrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.kalibyte.YashTools.workorder.repository.WorkOrderRepository;
import com.kalibyte.YashTools.workorder.entity.enums.WorkOrderStatus;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobCardServiceImpl implements JobCardService {

    private final JobCardRepository jobCardRepository;
    private final WorkOrderItemRepository workOrderItemRepository;
    private final WorkOrderRepository workOrderRepository;

    @Override
    @Transactional
    public JobCardResponse create(CreateJobCardRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        String companyCode = CompanyContextHolder.getCompanyCode();

        WorkOrderItem orderItem = workOrderItemRepository.findById(request.getWorkOrderItemId())
                .orElseThrow(() -> new BusinessException("Work Order Item not found with ID: " + request.getWorkOrderItemId()));
        
        if (!orderItem.getWorkOrder().getCompany().getId().equals(companyId)) {
            throw new BusinessException("Work Order Item does not belong to your company");
        }

        int scheduledQty = jobCardRepository.getSumQuantityByWorkOrderItemId(orderItem.getId(), companyId);
        int totalItemQty = orderItem.getQuantity();
        int remainingQty = totalItemQty - scheduledQty;

        int reqQty = request.getQuantity() != null ? request.getQuantity() : remainingQty;
        if (reqQty <= 0) {
            throw new BusinessException("No remaining quantity to plan for this item. Scheduled: " + scheduledQty + "/" + totalItemQty);
        }
        if (reqQty > remainingQty) {
            throw new BusinessException("Requested quantity " + reqQty + " exceeds remaining un-planned quantity " + remainingQty);
        }

        long seq = jobCardRepository.count() + 1;
        String jobCardNo = String.format("%s-JC-%d-%06d", companyCode, LocalDate.now().getYear(), seq);

        Company company = Company.builder().id(companyId).code(companyCode).build();
        JobCard jc = JobCard.builder()
                .jobCardNo(jobCardNo)
                .workOrder(orderItem.getWorkOrder())
                .workOrderItem(orderItem)
                .status(JobCardStatus.CREATED)
                .priority(request.getPriority() != null ? request.getPriority() : 1)
                .totalQuantity(reqQty)
                .remarks(request.getRemarks())
                .build();
        jc.setCompany(company);

        if (orderItem.getWorkOrder().getStatus() == WorkOrderStatus.CREATED) {
            orderItem.getWorkOrder().setStatus(WorkOrderStatus.IN_PROGRESS);
            workOrderRepository.save(orderItem.getWorkOrder());
        }

        JobCard saved = jobCardRepository.save(jc);
        log.info("Job Card {} created successfully", saved.getJobCardNo());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public List<JobCardResponse> split(SplitJobCardRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        String companyCode = CompanyContextHolder.getCompanyCode();

        JobCard parent = jobCardRepository.findByIdAndCompanyId(request.getJobCardId(), companyId)
                .orElseThrow(() -> new BusinessException("Job Card not found with ID: " + request.getJobCardId()));

        if (parent.getStatus() != JobCardStatus.CREATED && parent.getStatus() != JobCardStatus.PLANNED) {
            throw new BusinessException("Can only split job cards that are in CREATED or PLANNED status. Current status: " + parent.getStatus());
        }

        int totalSplit = request.getSplitQuantities().stream().mapToInt(Integer::intValue).sum();
        if (totalSplit != parent.getTotalQuantity()) {
            throw new BusinessException("Sum of split quantities (" + totalSplit + ") must equal original Job Card quantity (" + parent.getTotalQuantity() + ")");
        }

        List<JobCard> newJobCards = new ArrayList<>();
        int index = 1;
        for (int qty : request.getSplitQuantities()) {
            if (qty <= 0) {
                throw new BusinessException("Split quantity must be greater than zero");
            }
            long seq = jobCardRepository.count() + 1 + newJobCards.size();
            String jobCardNo = String.format("%s-JC-%d-%06d-%d", companyCode, LocalDate.now().getYear(), seq, index++);

            JobCard jc = JobCard.builder()
                    .jobCardNo(jobCardNo)
                    .workOrder(parent.getWorkOrder())
                    .workOrderItem(parent.getWorkOrderItem())
                    .status(parent.getStatus())
                    .priority(parent.getPriority())
                    .totalQuantity(qty)
                    .remarks(parent.getRemarks() + " (Split from " + parent.getJobCardNo() + ")")
                    .isRework(parent.getIsRework())
                    .reworkParentJobCard(parent.getReworkParentJobCard())
                    .build();
            jc.setCompany(parent.getCompany());
            newJobCards.add(jc);
        }

        parent.setStatus(JobCardStatus.CANCELLED);
        parent.setRemarks((parent.getRemarks() == null ? "" : parent.getRemarks() + "\n") 
                + "[CANCELLED] Split into: " + newJobCards.stream().map(JobCard::getJobCardNo).collect(Collectors.joining(", ")));
        jobCardRepository.save(parent);

        List<JobCard> saved = jobCardRepository.saveAll(newJobCards);
        log.info("Job Card {} split into {} child cards", parent.getJobCardNo(), saved.size());
        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JobCardResponse getById(UUID id) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        JobCard jc = jobCardRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Job Card not found with ID: " + id));
        return toResponse(jc);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobCardResponse> list(String status, Pageable pageable) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (status != null && !status.isBlank()) {
            try {
                JobCardStatus statusEnum = JobCardStatus.valueOf(status.toUpperCase());
                return jobCardRepository.findAll((root, query, cb) -> cb.and(
                        cb.equal(root.get("company").get("id"), companyId),
                        cb.equal(root.get("status"), statusEnum)
                ), pageable).map(this::toResponse);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid status: " + status);
            }
        }
        return jobCardRepository.findAll((root, query, cb) -> 
                cb.equal(root.get("company").get("id"), companyId), pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public JobCardResponse updatePriority(UUID id, Integer priority) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        JobCard jc = jobCardRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Job Card not found with ID: " + id));
        
        jc.setPriority(priority);
        JobCard saved = jobCardRepository.save(jc);
        log.info("Job Card {} priority updated to {}", saved.getJobCardNo(), priority);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public JobCardResponse hold(UUID id, String reason) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        JobCard jc = jobCardRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Job Card not found with ID: " + id));

        if (jc.getStatus() != JobCardStatus.PLANNED && jc.getStatus() != JobCardStatus.ASSIGNED && jc.getStatus() != JobCardStatus.STARTED) {
            throw new BusinessException("Cannot pause/hold job card in status: " + jc.getStatus());
        }

        jc.setStatus(JobCardStatus.PAUSED);
        jc.setRemarks((jc.getRemarks() == null ? "" : jc.getRemarks() + "\n") 
                + "[PAUSED] " + reason);
        JobCard saved = jobCardRepository.save(jc);
        log.info("Job Card {} put on hold/paused", saved.getJobCardNo());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public JobCardResponse cancel(UUID id, String reason) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        JobCard jc = jobCardRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Job Card not found with ID: " + id));

        if (jc.getStatus() == JobCardStatus.COMPLETED || jc.getStatus() == JobCardStatus.CANCELLED) {
            throw new BusinessException("Cannot cancel job card in status: " + jc.getStatus());
        }

        jc.setStatus(JobCardStatus.CANCELLED);
        jc.setRemarks((jc.getRemarks() == null ? "" : jc.getRemarks() + "\n") 
                + "[CANCELLED] " + reason);
        JobCard saved = jobCardRepository.save(jc);
        log.info("Job Card {} cancelled", saved.getJobCardNo());
        return toResponse(saved);
    }

    private JobCardResponse toResponse(JobCard jc) {
        WorkOrderItem item = jc.getWorkOrderItem();
        return JobCardResponse.builder()
                .id(jc.getId())
                .jobCardNo(jc.getJobCardNo())
                .workOrderId(jc.getWorkOrder().getId())
                .workOrderNo(jc.getWorkOrder().getWorkOrderNo())
                .workOrderItemId(item.getId())
                .lineNumber(item.getLineNumber())
                .toolName(item.getToolName())
                .itemName(item.getItemName())
                .status(jc.getStatus().name())
                .priority(jc.getPriority())
                .totalQuantity(jc.getTotalQuantity())
                .itemQuantity(item.getQuantity())
                .diameter(item.getDiameter())
                .shankDiameter(item.getShankDiameter())
                .overallLength(item.getOverallLength())
                .fluteLength(item.getFluteLength())
                .drawingReference(item.getDrawingReference())
                .materialGrade(item.getMaterialGrade() != null ? item.getMaterialGrade().name() : null)
                .materialType(item.getMaterialType())
                .coatingType(item.getCoatingType())
                .technicalNotes(item.getTechnicalNotes())
                .remarks(jc.getRemarks())
                .isRework(jc.getIsRework())
                .reworkParentJobCardId(jc.getReworkParentJobCard() != null ? jc.getReworkParentJobCard().getId() : null)
                .reworkParentJobCardNo(jc.getReworkParentJobCard() != null ? jc.getReworkParentJobCard().getJobCardNo() : null)
                .createdAt(jc.getCreatedAt())
                .createdBy(jc.getCreatedBy())
                .companyId(jc.getCompany().getId())
                .build();
    }
}
