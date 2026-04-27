package com.example.YashToolBackEnd.auth.service;

import com.example.YashToolBackEnd.auth.dto.*;
import com.example.YashToolBackEnd.auth.entity.Role;
import com.example.YashToolBackEnd.auth.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void createUser(UserRegistrationRequest request);

    void changePassword(ChangePasswordRequest request);

    List<Role> getRoles();

    List<User> getAllUsers();

    UserResponse getUserById(UUID id);

    void deleteUser(UUID id);

    void disableUser(UUID id);

    void enableUser(UUID id);

    Page<UserResponse> getAllUsers(int page, int size);
}
