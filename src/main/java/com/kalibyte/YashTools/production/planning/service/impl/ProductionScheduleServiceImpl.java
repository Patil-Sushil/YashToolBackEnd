package com.kalibyte.YashTools.production.planning.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.labors.labor.entity.Laborer;
import com.kalibyte.YashTools.labors.labor.repository.LaborerRepository;
import com.kalibyte.YashTools.production.execution.repository.ExecutionLogRepository;
import com.kalibyte.YashTools.production.jobcard.entity.JobCard;
import com.kalibyte.YashTools.production.jobcard.entity.enums.JobCardStatus;
import com.kalibyte.YashTools.production.jobcard.repository.JobCardRepository;
import com.kalibyte.YashTools.production.machine.entity.Machine;
import com.kalibyte.YashTools.production.machine.entity.enums.MachineStatus;
import com.kalibyte.YashTools.production.machine.repository.MachineRepository;
import com.kalibyte.YashTools.production.planning.dto.ScheduleRequest;
import com.kalibyte.YashTools.production.planning.dto.ScheduleResponse;
import com.kalibyte.YashTools.production.planning.entity.ProductionSchedule;
import com.kalibyte.YashTools.production.planning.entity.enums.ScheduleStatus;
import com.kalibyte.YashTools.production.planning.entity.enums.ShiftType;
import com.kalibyte.YashTools.production.planning.repository.ProductionScheduleRepository;
import com.kalibyte.YashTools.production.planning.service.ProductionScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.kalibyte.YashTools.workorder.repository.WorkOrderRepository;
import com.kalibyte.YashTools.workorder.entity.enums.WorkOrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionScheduleServiceImpl implements ProductionScheduleService {

    private final ProductionScheduleRepository scheduleRepository;
    private final JobCardRepository jobCardRepository;
    private final MachineRepository machineRepository;
    private final LaborerRepository laborerRepository;
    private final ExecutionLogRepository executionLogRepository;
    private final WorkOrderRepository workOrderRepository;

    @Override
    @Transactional
    public ScheduleResponse schedule(ScheduleRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();

        JobCard jc = jobCardRepository.findByIdAndCompanyId(request.getJobCardId(), companyId)
                .orElseThrow(() -> new BusinessException("Job Card not found with ID: " + request.getJobCardId()));

        if (jc.getStatus() != JobCardStatus.CREATED && jc.getStatus() != JobCardStatus.PLANNED) {
            throw new BusinessException("Can only schedule job cards that are in CREATED or PLANNED status. Current status: " + jc.getStatus());
        }

        if (scheduleRepository.existsByJobCardId(jc.getId())) {
            throw new BusinessException("Production schedule already exists for Job Card: " + jc.getJobCardNo());
        }

        Machine machine = machineRepository.findByIdAndCompanyId(request.getMachineId(), companyId)
                .orElseThrow(() -> new BusinessException("Machine not found with ID: " + request.getMachineId()));

        if (machine.getStatus() != MachineStatus.ACTIVE) {
            throw new BusinessException("Machine is not active. Current status: " + machine.getStatus());
        }

        Laborer operator = laborerRepository.findById(request.getOperatorId())
                .orElseThrow(() -> new BusinessException("Operator not found with ID: " + request.getOperatorId()));

        if (Boolean.FALSE.equals(operator.getIsActive())) {
            throw new BusinessException("Operator is inactive");
        }

        ShiftType shift;
        try {
            shift = ShiftType.valueOf(request.getShift().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid shift type: " + request.getShift());
        }

        if (request.getPlannedStartDate().isAfter(request.getPlannedEndDate())) {
            throw new BusinessException("Planned start date must be before or equal to end date");
        }

        ProductionSchedule ps = ProductionSchedule.builder()
                .jobCard(jc)
                .machine(machine)
                .operator(operator)
                .shift(shift)
                .plannedStartDate(request.getPlannedStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .status(ScheduleStatus.PENDING)
                .build();
        ps.setCompany(jc.getCompany());

        jc.setStatus(JobCardStatus.PLANNED);
        jobCardRepository.save(jc);

        if (jc.getWorkOrder() != null && jc.getWorkOrder().getStatus() == WorkOrderStatus.CREATED) {
            jc.getWorkOrder().setStatus(WorkOrderStatus.IN_PROGRESS);
            workOrderRepository.save(jc.getWorkOrder());
        }

        ProductionSchedule saved = scheduleRepository.save(ps);
        log.info("Production schedule created for Job Card: {}", jc.getJobCardNo());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ScheduleResponse reschedule(UUID id, ScheduleRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();

        ProductionSchedule ps = scheduleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Production schedule not found with ID: " + id));

        JobCard jc = ps.getJobCard();
        if (jc.getStatus() == JobCardStatus.COMPLETED || jc.getStatus() == JobCardStatus.CANCELLED) {
            throw new BusinessException("Cannot reschedule job cards in COMPLETED or CANCELLED status");
        }

        Machine machine = machineRepository.findByIdAndCompanyId(request.getMachineId(), companyId)
                .orElseThrow(() -> new BusinessException("Machine not found with ID: " + request.getMachineId()));

        if (machine.getStatus() != MachineStatus.ACTIVE) {
            throw new BusinessException("Machine is not active. Current status: " + machine.getStatus());
        }

        Laborer operator = laborerRepository.findById(request.getOperatorId())
                .orElseThrow(() -> new BusinessException("Operator not found with ID: " + request.getOperatorId()));

        if (Boolean.FALSE.equals(operator.getIsActive())) {
            throw new BusinessException("Operator is inactive");
        }

        ShiftType shift;
        try {
            shift = ShiftType.valueOf(request.getShift().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid shift type: " + request.getShift());
        }

        if (request.getPlannedStartDate().isAfter(request.getPlannedEndDate())) {
            throw new BusinessException("Planned start date must be before or equal to end date");
        }

        ps.setMachine(machine);
        ps.setOperator(operator);
        ps.setShift(shift);
        ps.setPlannedStartDate(request.getPlannedStartDate());
        ps.setPlannedEndDate(request.getPlannedEndDate());

        ProductionSchedule saved = scheduleRepository.save(ps);
        log.info("Production schedule rescheduled for Job Card: {}", jc.getJobCardNo());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponse getById(UUID id) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        ProductionSchedule ps = scheduleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Production schedule not found with ID: " + id));
        return toResponse(ps);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponse getByJobCardId(UUID jobCardId) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        ProductionSchedule ps = scheduleRepository.findByJobCardIdAndCompanyId(jobCardId, companyId)
                .orElseThrow(() -> new BusinessException("Production schedule not found for Job Card: " + jobCardId));
        return toResponse(ps);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScheduleResponse> list(Pageable pageable) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        return scheduleRepository.findAll((root, query, cb) -> 
                cb.equal(root.get("company").get("id"), companyId), pageable)
                .map(this::toResponse);
    }

    private ScheduleResponse toResponse(ProductionSchedule ps) {
        ScheduleStatus effectiveStatus = ps.getStatus();
        if (ps.getJobCard() != null) {
            JobCardStatus jcStatus = ps.getJobCard().getStatus();
            if (jcStatus == JobCardStatus.COMPLETED) {
                effectiveStatus = ScheduleStatus.COMPLETED;
            } else if (jcStatus == JobCardStatus.PAUSED) {
                effectiveStatus = ScheduleStatus.PAUSED;
            } else if (jcStatus == JobCardStatus.STARTED) {
                effectiveStatus = ScheduleStatus.RUNNING;
            } else if (executionLogRepository.findByJobCardIdAndEndTimeIsNull(ps.getJobCard().getId()).isPresent()) {
                effectiveStatus = ScheduleStatus.RUNNING;
            }
        }

        com.kalibyte.YashTools.workorder.entity.WorkOrderItem item = ps.getJobCard().getWorkOrderItem();
        return ScheduleResponse.builder()
                .id(ps.getId())
                .jobCardId(ps.getJobCard().getId())
                .jobCardNo(ps.getJobCard().getJobCardNo())
                .totalQuantity(ps.getJobCard().getTotalQuantity())
                .machineId(ps.getMachine().getId())
                .machineCode(ps.getMachine().getCode())
                .machineName(ps.getMachine().getName())
                .operatorId(ps.getOperator().getId())
                .operatorName(ps.getOperator().getName())
                .shift(ps.getShift().name())
                .plannedStartDate(ps.getPlannedStartDate())
                .plannedEndDate(ps.getPlannedEndDate())
                .status(effectiveStatus.name())
                .createdAt(ps.getCreatedAt())
                .createdBy(ps.getCreatedBy())
                .companyId(ps.getCompany().getId())
                .priority(ps.getJobCard().getPriority())
                .workOrderId(ps.getJobCard().getWorkOrder().getId())
                .workOrderNo(ps.getJobCard().getWorkOrder().getWorkOrderNo())
                .toolName(item.getToolName())
                .itemName(item.getItemName())
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
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<ScheduleResponse> getSchedulesByMachine(UUID machineId) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        return scheduleRepository.findByMachineIdAndCompanyId(machineId, companyId).stream()
                .sorted((s1, s2) -> {
                    int pComp = s2.getJobCard().getPriority().compareTo(s1.getJobCard().getPriority());
                    if (pComp != 0) return pComp;
                    return s1.getPlannedStartDate().compareTo(s2.getPlannedStartDate());
                })
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void updatePriorities(com.kalibyte.YashTools.production.planning.dto.UpdatePlanningPrioritiesRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        for (com.kalibyte.YashTools.production.planning.dto.UpdatePlanningPrioritiesRequest.Item item : request.getItems()) {
            JobCard jc = jobCardRepository.findByIdAndCompanyId(item.getJobCardId(), companyId)
                    .orElseThrow(() -> new BusinessException("Job Card not found with ID: " + item.getJobCardId()));
            jc.setPriority(item.getPriority());
            jobCardRepository.save(jc);
            log.info("Updated Job Card {} priority to {}", jc.getJobCardNo(), item.getPriority());
        }
    }
}

