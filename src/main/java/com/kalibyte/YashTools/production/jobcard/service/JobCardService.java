package com.kalibyte.YashTools.production.jobcard.service;

import com.kalibyte.YashTools.production.jobcard.dto.CreateJobCardRequest;
import com.kalibyte.YashTools.production.jobcard.dto.JobCardResponse;
import com.kalibyte.YashTools.production.jobcard.dto.SplitJobCardRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface JobCardService {
    JobCardResponse create(CreateJobCardRequest request);
    List<JobCardResponse> split(SplitJobCardRequest request);
    JobCardResponse getById(UUID id);
    Page<JobCardResponse> list(String status, Pageable pageable);
    JobCardResponse updatePriority(UUID id, Integer priority);
    JobCardResponse hold(UUID id, String reason);
    JobCardResponse cancel(UUID id, String reason);
}
