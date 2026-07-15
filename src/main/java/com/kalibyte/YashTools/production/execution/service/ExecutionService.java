package com.kalibyte.YashTools.production.execution.service;

import com.kalibyte.YashTools.production.execution.dto.ExecutionResponse;
import com.kalibyte.YashTools.production.execution.dto.StartJobRequest;
import com.kalibyte.YashTools.production.execution.dto.UpdateProgressRequest;

import java.util.List;
import java.util.UUID;

public interface ExecutionService {
    ExecutionResponse startJob(StartJobRequest request);
    ExecutionResponse updateProgress(UUID logId, UpdateProgressRequest request);
    ExecutionResponse getActiveLogByJobCard(UUID jobCardId);
    ExecutionResponse getActiveLogByOperator(Long operatorId);
    List<ExecutionResponse> getLogsByJobCard(UUID jobCardId);
}
