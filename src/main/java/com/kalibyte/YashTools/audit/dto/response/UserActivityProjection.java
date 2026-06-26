package com.kalibyte.YashTools.audit.dto.response;


import java.time.LocalDateTime;
import java.util.UUID;

public interface UserActivityProjection {
    UUID getUserId();
    String getUsername();
    Long getTotalActions();
    LocalDateTime getLastActivity();
    LocalDateTime getFirstActivity();
}

