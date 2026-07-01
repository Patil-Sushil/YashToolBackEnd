package com.kalibyte.YashTools.auth.controller;


import com.kalibyte.YashTools.auth.dto.request.ChangePasswordRequest;
import com.kalibyte.YashTools.auth.dto.request.LoginRequest;
import com.kalibyte.YashTools.auth.dto.request.LogoutRequest;
import com.kalibyte.YashTools.auth.dto.request.TokenRefreshRequest;
import com.kalibyte.YashTools.auth.dto.response.LoginResponse;
import com.kalibyte.YashTools.auth.dto.response.TokenRefreshResponse;
import com.kalibyte.YashTools.auth.service.AuthService;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
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
    @LoggableAction(value = "User authentication (login)", action = AuditAction.LOGIN, entityType = "AUTH")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @LoggableAction(value = "Token refresh", action = AuditAction.TOKEN_REFRESHED, entityType = "AUTH")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @LoggableAction(value = "User logout", action = AuditAction.LOGOUT, entityType = "AUTH")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @PostMapping("/change-password")
    @LoggableAction(value = "Password change", action = AuditAction.PASSWORD_CHANGED, entityType = "USER")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully", null)
        );
    }
}
