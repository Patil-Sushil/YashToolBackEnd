package com.kalibyte.YashTools.inventory.transaction.materialissue.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.transaction.materialissue.dto.MaterialIssueRequest;
import com.kalibyte.YashTools.inventory.transaction.materialissue.dto.MaterialIssueResponse;

import java.util.UUID;

public interface MaterialIssueService {
    MaterialIssueResponse issueMaterial(MaterialIssueRequest request);
    MaterialIssueResponse getIssueById(UUID id);
    MaterialIssueResponse getIssueByNumber(String issueNumber);
    PageResponse<MaterialIssueResponse> getAllIssues(int page, int size);
}
