package com.kalibyte.YashTools.auth.controller;


import com.kalibyte.YashTools.auth.dto.ChangePasswordRequest;
import com.kalibyte.YashTools.auth.dto.LoginRequest;
import com.kalibyte.YashTools.auth.dto.LoginResponse;
import com.kalibyte.YashTools.auth.service.AuthService;
import com.kalibyte.YashTools.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully", null)
        );
    }
}
