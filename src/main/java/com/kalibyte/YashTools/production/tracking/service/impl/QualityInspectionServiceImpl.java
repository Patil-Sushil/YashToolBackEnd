package com.kalibyte.YashTools.production.tracking.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.production.execution.entity.ExecutionLog;
import com.kalibyte.YashTools.production.execution.repository.ExecutionLogRepository;
import com.kalibyte.YashTools.production.finishedgoods.service.FinishedGoodsStockService;
import com.kalibyte.YashTools.production.jobcard.entity.JobCard;
import com.kalibyte.YashTools.production.jobcard.entity.enums.JobCardStatus;
import com.kalibyte.YashTools.production.jobcard.repository.JobCardRepository;
import com.kalibyte.YashTools.production.machine.entity.Machine;
import com.kalibyte.YashTools.production.machine.entity.enums.MachineStatus;
import com.kalibyte.YashTools.production.machine.repository.MachineRepository;
import com.kalibyte.YashTools.production.planning.entity.enums.ScheduleStatus;
import com.kalibyte.YashTools.production.planning.repository.ProductionScheduleRepository;
import com.kalibyte.YashTools.production.tracking.dto.PlannerDashboardResponse;
import com.kalibyte.YashTools.production.tracking.dto.QualityInspectionRequest;
import com.kalibyte.YashTools.production.tracking.dto.QualityInspectionResponse;
import com.kalibyte.YashTools.production.tracking.entity.QualityInspection;
import com.kalibyte.YashTools.production.tracking.entity.enums.InspectionResult;
import com.kalibyte.YashTools.production.tracking.repository.QualityInspectionRepository;
import com.kalibyte.YashTools.production.tracking.service.QualityInspectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QualityInspectionServiceImpl implements QualityInspectionService {

    private final QualityInspectionRepository qualityInspectionRepository;
    private final JobCardRepository jobCardRepository;
    private final ExecutionLogRepository executionLogRepository;
    private final ProductionScheduleRepository scheduleRepository;
    private final MachineRepository machineRepository;
    private final FinishedGoodsStockService finishedGoodsStockService;

    @Override
    @Transactional
    public QualityInspectionResponse inspect(QualityInspectionRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        String companyCode = CompanyContextHolder.getCompanyCode();

        JobCard jc = jobCardRepository.findByIdAndCompanyId(request.getJobCardId(), companyId)
                .orElseThrow(() -> new BusinessException("Job Card not found with ID: " + request.getJobCardId()));

        if (jc.getStatus() == JobCardStatus.CREATED || jc.getStatus() == JobCardStatus.PLANNED || jc.getStatus() == JobCardStatus.CANCELLED) {
            throw new BusinessException("Cannot record QC inspection for Job Card in status: " + jc.getStatus());
        }

        if (qualityInspectionRepository.existsByJobCardId(jc.getId())) {
            throw new BusinessException("Quality inspection is already recorded for Job Card: " + jc.getJobCardNo());
        }

        List<ExecutionLog> logs = executionLogRepository.findByJobCardId(jc.getId());
        int totalProduced = logs.stream().mapToInt(ExecutionLog::getProducedQuantity).sum();
        int requestedTotal = request.getAcceptedQuantity() + request.getRejectedQuantity() + request.getReworkQuantity();

        if (requestedTotal != totalProduced) {
            throw new BusinessException("Sum of accepted, rejected, and rework quantities (" + requestedTotal 
                    + ") must match total produced quantity from shop floor execution (" + totalProduced + ")");
        }

        InspectionResult result = (request.getAcceptedQuantity() == totalProduced) ? InspectionResult.PASS : InspectionResult.REJECT;

        QualityInspection qi = QualityInspection.builder()
                .jobCard(jc)
                .acceptedQuantity(request.getAcceptedQuantity())
                .rejectedQuantity(request.getRejectedQuantity())
                .reworkQuantity(request.getReworkQuantity())
                .inspector(request.getInspector())
                .inspectionDate(LocalDateTime.now())
                .result(result)
                .remarks(request.getRemarks())
                .build();
        qi.setCompany(jc.getCompany());

        QualityInspection saved = qualityInspectionRepository.save(qi);

        // Auto-increment finished goods inventory upon a successful PASS or any accepted quantity
        if (request.getAcceptedQuantity() > 0) {
            finishedGoodsStockService.addFinishedGoodsStock(jc.getWorkOrderItem(), request.getAcceptedQuantity());
        }
        // Auto-create Rework Job Card if reworkQuantity > 0
        if (request.getReworkQuantity() > 0) {
            long seq = jobCardRepository.count() + 1;
            String reworkNo = String.format("%s-JC-RW-%d-%06d", companyCode, LocalDate.now().getYear(), seq);

            JobCard reworkCard = JobCard.builder()
                    .jobCardNo(reworkNo)
                    .workOrder(jc.getWorkOrder())
                    .workOrderItem(jc.getWorkOrderItem())
                    .status(JobCardStatus.CREATED)
                    .priority(jc.getPriority() + 1) // Higher priority for rework
                    .totalQuantity(request.getReworkQuantity())
                    .remarks("Auto-generated Rework Job Card from parent: " + jc.getJobCardNo() + ". QC Remarks: " + request.getRemarks())
                    .isRework(true)
                    .reworkParentJobCard(jc)
                    .build();
            reworkCard.setCompany(jc.getCompany());
            jobCardRepository.save(reworkCard);
            log.info("Auto-created Rework Job Card {} for quantity {}", reworkNo, request.getReworkQuantity());
        }

        log.info("QC Inspection recorded for Job Card {}. Result: {}", jc.getJobCardNo(), result);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public QualityInspectionResponse getById(UUID id) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        QualityInspection qi = qualityInspectionRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("QC Inspection not found with ID: " + id));
        return toResponse(qi);
    }

    @Override
    @Transactional(readOnly = true)
    public QualityInspectionResponse getByJobCardId(UUID jobCardId) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        QualityInspection qi = qualityInspectionRepository.findByJobCardIdAndCompanyId(jobCardId, companyId)
                .orElseThrow(() -> new BusinessException("QC Inspection not found for Job Card: " + jobCardId));
        return toResponse(qi);
    }

    @Override
    @Transactional(readOnly = true)
    public PlannerDashboardResponse getDashboard() {
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

        return PlannerDashboardResponse.builder()
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

    private QualityInspectionResponse toResponse(QualityInspection qi) {
        return QualityInspectionResponse.builder()
                .id(qi.getId())
                .jobCardId(qi.getJobCard().getId())
                .jobCardNo(qi.getJobCard().getJobCardNo())
                .totalQuantity(qi.getJobCard().getTotalQuantity())
                .acceptedQuantity(qi.getAcceptedQuantity())
                .rejectedQuantity(qi.getRejectedQuantity())
                .reworkQuantity(qi.getReworkQuantity())
                .inspector(qi.getInspector())
                .inspectionDate(qi.getInspectionDate())
                .result(qi.getResult().name())
                .remarks(qi.getRemarks())
                .companyId(qi.getCompany().getId())
                .build();
    }
}
