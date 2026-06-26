package com.kalibyte.YashTools.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityResponse {
    private UUID userId;
    private String username;
    private Long totalActions;
    private LocalDateTime lastActivity;
    private LocalDateTime firstActivity;
    private List<ActionCount> topActions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionCount {
        private String action;
        private Long count;
    }
}
