package com.example.YashToolBackEnd.auth.controller;


import com.example.YashToolBackEnd.auth.dto.ChangePasswordRequest;
import com.example.YashToolBackEnd.auth.dto.LoginRequest;
import com.example.YashToolBackEnd.auth.dto.LoginResponse;
import com.example.YashToolBackEnd.auth.service.AuthService;
import com.example.YashToolBackEnd.common.response.ApiResponse;
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
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(request);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Password changed successfully", null)
        );
    }
}
