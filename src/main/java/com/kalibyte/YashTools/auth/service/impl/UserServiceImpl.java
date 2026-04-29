package com.kalibyte.YashTools.auth.service.impl;

import com.kalibyte.YashTools.auth.entity.User;
import com.kalibyte.YashTools.auth.repository.UserRepository;
import com.kalibyte.YashTools.auth.service.UserService;
import com.kalibyte.YashTools.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
    }
}
