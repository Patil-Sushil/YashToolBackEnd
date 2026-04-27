package com.example.YashToolBackEnd.auth.service.impl;

import com.example.YashToolBackEnd.auth.entity.User;
import com.example.YashToolBackEnd.auth.repository.UserRepository;
import com.example.YashToolBackEnd.auth.service.UserService;
import com.example.YashToolBackEnd.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
    }
}
