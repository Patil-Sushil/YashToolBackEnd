package com.kalibyte.YashTools.auth.controller;

import com.kalibyte.YashTools.auth.dto.UserRegistrationRequest;
import com.kalibyte.YashTools.auth.dto.UserResponse;
import com.kalibyte.YashTools.auth.service.AuthService;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UserManagementController {

    private final AuthService authService;

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    @LoggableAction(value = "Create User", action = AuditAction.USER_CREATED, entityType = "USER")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse createdUser = authService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", createdUser));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @LoggableAction(value = "Retrieve All Users", action = AuditAction.GET_ALL_USERS, entityType = "USER")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", authService.getAllUsers(page, size))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LoggableAction(value = "Retrieve User By ID", action = AuditAction.GET_USER_BY_ID, entityType = "USER")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {

        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", authService.getUserById(id))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LoggableAction(value = "Delete User", action = AuditAction.USER_DELETED, entityType = "USER")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {

        authService.deleteUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully", null)
        );
    }

    // Endpoint to disable a user account
    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @LoggableAction(value = "Disable User", action = AuditAction.USER_DISABLED, entityType = "USER")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable UUID id) {

        authService.disableUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("User disabled successfully", null)
        );
    }

    // Endpoint to enable a user account
    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @LoggableAction(value = "Enable User", action = AuditAction.USER_ENABLED, entityType = "USER")
    public ResponseEntity<ApiResponse<Void>> enableUser(@PathVariable UUID id) {

        authService.enableUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("User enable successfully", null)
        );
    }

}
