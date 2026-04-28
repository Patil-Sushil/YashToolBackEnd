package com.kalibyte.YashTools.auth.service;

import com.kalibyte.YashTools.auth.entity.User;

public interface UserService {

    User getByEmail(String email);
}
