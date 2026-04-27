package com.example.YashToolBackEnd.auth.service;

import com.example.YashToolBackEnd.auth.entity.User;

public interface UserService {

    User getByEmail(String email);
}
