package com.kalibyte.YashTools.auth.service;

import com.kalibyte.YashTools.auth.dto.*;
import com.kalibyte.YashTools.auth.entity.Role;
import com.kalibyte.YashTools.auth.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserResponse createUser(UserRegistrationRequest request);

    void changePassword(ChangePasswordRequest request);

    List<Role> getRoles();

    List<User> getAllUsers();

    UserResponse getUserById(UUID id);

    void deleteUser(UUID id);

    void disableUser(UUID id);

    void enableUser(UUID id);

    Page<UserResponse> getAllUsers(int page, int size);
}
