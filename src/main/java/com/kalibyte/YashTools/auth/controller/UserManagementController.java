package com.kalibyte.YashTools.auth.controller;

import com.kalibyte.YashTools.auth.dto.UserRegistrationRequest;
import com.kalibyte.YashTools.auth.dto.UserResponse;
import com.kalibyte.YashTools.auth.service.AuthService;
import com.kalibyte.YashTools.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse createdUser = authService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "User created successfully", createdUser));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Users retrieved successfully",
                        authService.getAllUsers(page, size))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {

        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "User retrieved successfully",
                        authService.getUserById(id))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {

        authService.deleteUser(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "User deleted successfully",
                        null)
        );
    }

    // Endpoint to disable a user account
    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable UUID id) {

        authService.disableUser(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "User disabled successfully", null)
        );
    }

    // Endpoint to enable a user account
    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> enableUser(@PathVariable UUID id) {

        authService.enableUser(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "User enable successfully", null)
        );
    }

}
