package com.novabank.user.service;

import com.novabank.user.dto.request.ChangePasswordRequest;
import com.novabank.user.dto.request.LoginRequest;
import com.novabank.user.dto.request.UserProfileUpdateRequest;
import com.novabank.user.dto.request.UserRegistrationRequest;
import com.novabank.user.dto.response.LoginResponse;
import com.novabank.user.dto.response.UserRegistrationResponse;

import java.util.List;

public interface UserService {

    UserRegistrationResponse register(UserRegistrationRequest request);

    LoginResponse login(LoginRequest request);

    UserRegistrationResponse getProfile(String email);

    UserRegistrationResponse updateProfile(String email, UserProfileUpdateRequest request);

    String changePassword(String email, ChangePasswordRequest request);
}
