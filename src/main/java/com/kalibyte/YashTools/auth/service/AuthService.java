package com.kalibyte.YashTools.auth.service;

import com.kalibyte.YashTools.auth.dto.request.*;
import com.kalibyte.YashTools.auth.dto.response.LoginResponse;
import com.kalibyte.YashTools.auth.dto.response.TokenRefreshResponse;
import com.kalibyte.YashTools.auth.dto.response.UserResponse;
import com.kalibyte.YashTools.auth.entity.Role;
import com.kalibyte.YashTools.auth.entity.User;

import java.util.List;
import java.util.UUID;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    TokenRefreshResponse refreshToken(TokenRefreshRequest request);

    void logout(LogoutRequest request);

    UserResponse createUser(UserRegistrationRequest request);

    void changePassword(ChangePasswordRequest request);

    List<Role> getRoles();

    List<User> getAllUsers();

    UserResponse getUserById(UUID id);

    void deleteUser(UUID id);

    void disableUser(UUID id);

    void enableUser(UUID id);

    com.kalibyte.YashTools.common.response.PageResponse<UserResponse> getAllUsers(int page, int size);
}
