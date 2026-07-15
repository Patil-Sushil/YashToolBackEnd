package com.kalibyte.YashTools.production.execution.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.labors.labor.entity.Laborer;
import com.kalibyte.YashTools.labors.labor.repository.LaborerRepository;
import com.kalibyte.YashTools.production.execution.dto.ExecutionResponse;
import com.kalibyte.YashTools.production.execution.dto.StartJobRequest;
import com.kalibyte.YashTools.production.execution.dto.UpdateProgressRequest;
import com.kalibyte.YashTools.production.execution.entity.ExecutionLog;
import com.kalibyte.YashTools.production.execution.repository.ExecutionLogRepository;
import com.kalibyte.YashTools.production.execution.service.ExecutionService;
import com.kalibyte.YashTools.production.jobcard.entity.JobCard;
import com.kalibyte.YashTools.production.jobcard.entity.enums.JobCardStatus;
import com.kalibyte.YashTools.production.jobcard.repository.JobCardRepository;
import com.kalibyte.YashTools.production.machine.entity.Machine;
import com.kalibyte.YashTools.production.machine.entity.enums.MachineStatus;
import com.kalibyte.YashTools.production.machine.repository.MachineRepository;
import com.kalibyte.YashTools.production.planning.entity.enums.ShiftType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionServiceImpl implements ExecutionService {

    private final ExecutionLogRepository executionLogRepository;
    private final JobCardRepository jobCardRepository;
    private final MachineRepository machineRepository;
    private final LaborerRepository laborerRepository;

    @Override
    @Transactional
    public ExecutionResponse startJob(StartJobRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();

        JobCard jc = jobCardRepository.findByIdAndCompanyId(request.getJobCardId(), companyId)
                .orElseThrow(() -> new BusinessException("Job Card not found with ID: " + request.getJobCardId()));

        if (jc.getStatus() == JobCardStatus.COMPLETED || jc.getStatus() == JobCardStatus.CANCELLED) {
            throw new BusinessException("Cannot start job card in its current status: " + jc.getStatus());
        }

        if (executionLogRepository.findByJobCardIdAndEndTimeIsNull(jc.getId()).isPresent()) {
            throw new BusinessException("Job Card is already running in another session");
        }

        if (executionLogRepository.findByOperatorIdAndEndTimeIsNull(request.getOperatorId()).isPresent()) {
            throw new BusinessException("Operator already has an active running job session");
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

        if (request.getTargetQuantity() <= 0) {
            throw new BusinessException("Target quantity must be greater than zero");
        }

        ExecutionLog logEntry = ExecutionLog.builder()
                .jobCard(jc)
                .operator(operator)
                .machine(machine)
                .shift(shift)
                .startTime(LocalDateTime.now())
                .targetQuantity(request.getTargetQuantity())
                .producedQuantity(0)
                .rejectedQuantity(0)
                .reworkQuantity(0)
                .pendingQuantity(request.getTargetQuantity())
                .machineDowntimeMinutes(0)
                .build();

        jc.setStatus(JobCardStatus.STARTED);
        jobCardRepository.save(jc);

        ExecutionLog saved = executionLogRepository.save(logEntry);
        log.info("Job started for Job Card {} by operator {}", jc.getJobCardNo(), operator.getName());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ExecutionResponse updateProgress(UUID logId, UpdateProgressRequest request) {
        ExecutionLog entry = executionLogRepository.findById(logId)
                .orElseThrow(() -> new BusinessException("Execution log not found with ID: " + logId));

        if (entry.getEndTime() != null) {
            throw new BusinessException("Cannot update a completed shift execution log");
        }

        if (request.getProducedQuantity() < 0 || request.getRejectedQuantity() < 0 || request.getReworkQuantity() < 0) {
            throw new BusinessException("Quantities cannot be negative");
        }

        entry.setProducedQuantity(request.getProducedQuantity());
        entry.setRejectedQuantity(request.getRejectedQuantity());
        entry.setReworkQuantity(request.getReworkQuantity());
        
        int pending = entry.getTargetQuantity() - request.getProducedQuantity();
        entry.setPendingQuantity(Math.max(0, pending));

        if (request.getMachineDowntimeMinutes() != null) {
            if (request.getMachineDowntimeMinutes() < 0) {
                throw new BusinessException("Machine downtime cannot be negative");
            }
            entry.setMachineDowntimeMinutes(request.getMachineDowntimeMinutes());
        }
        if (request.getDowntimeReason() != null) {
            entry.setDowntimeReason(request.getDowntimeReason());
        }
        if (request.getRemarks() != null) {
            entry.setRemarks(request.getRemarks());
        }

        JobCard jc = entry.getJobCard();

        if (Boolean.TRUE.equals(request.getIsShiftComplete())) {
            entry.setEndTime(LocalDateTime.now());
            
            // Calculate total produced quantity across all logs for this job card
            List<ExecutionLog> logs = executionLogRepository.findByJobCardId(jc.getId());
            int totalProduced = logs.stream().mapToInt(ExecutionLog::getProducedQuantity).sum();
            
            if (totalProduced >= jc.getTotalQuantity()) {
                jc.setStatus(JobCardStatus.COMPLETED);
            } else {
                jc.setStatus(JobCardStatus.PAUSED);
            }
            jobCardRepository.save(jc);
        }

        ExecutionLog saved = executionLogRepository.save(entry);
        log.info("Execution progress updated for log ID {}", logId);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionResponse getActiveLogByJobCard(UUID jobCardId) {
        ExecutionLog entry = executionLogRepository.findByJobCardIdAndEndTimeIsNull(jobCardId)
                .orElseThrow(() -> new BusinessException("No active running session found for Job Card ID: " + jobCardId));
        return toResponse(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionResponse getActiveLogByOperator(Long operatorId) {
        ExecutionLog entry = executionLogRepository.findByOperatorIdAndEndTimeIsNull(operatorId)
                .orElseThrow(() -> new BusinessException("No active running session found for Operator ID: " + operatorId));
        return toResponse(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionResponse> getLogsByJobCard(UUID jobCardId) {
        return executionLogRepository.findByJobCardId(jobCardId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ExecutionResponse toResponse(ExecutionLog entry) {
        return ExecutionResponse.builder()
                .id(entry.getId())
                .jobCardId(entry.getJobCard().getId())
                .jobCardNo(entry.getJobCard().getJobCardNo())
                .operatorId(entry.getOperator().getId())
                .operatorName(entry.getOperator().getName())
                .machineId(entry.getMachine().getId())
                .machineCode(entry.getMachine().getCode())
                .shift(entry.getShift().name())
                .startTime(entry.getStartTime())
                .endTime(entry.getEndTime())
                .targetQuantity(entry.getTargetQuantity())
                .producedQuantity(entry.getProducedQuantity())
                .rejectedQuantity(entry.getRejectedQuantity())
                .reworkQuantity(entry.getReworkQuantity())
                .pendingQuantity(entry.getPendingQuantity())
                .machineDowntimeMinutes(entry.getMachineDowntimeMinutes())
                .downtimeReason(entry.getDowntimeReason())
                .remarks(entry.getRemarks())
                .createdAt(entry.getCreatedAt())
                .createdBy(entry.getCreatedBy())
                .build();
    }
}
