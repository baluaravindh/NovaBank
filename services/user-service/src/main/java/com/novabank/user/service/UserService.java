package com.novabank.user.service;

import com.novabank.user.dto.request.*;
import com.novabank.user.dto.response.LoginResponse;
import com.novabank.user.dto.response.UserRegistrationResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserRegistrationResponse register(UserRegistrationRequest request);

    LoginResponse login(LoginRequest request);

    UserRegistrationResponse getProfile(String email);

    UserRegistrationResponse updateProfile(String email, UserProfileUpdateRequest request);

    String changePassword(String email, ChangePasswordRequest request);

    String forgotPassword(ForgotPasswordRequest request);

    String resetPassword(ResetPasswordRequest request);

    void activateUser(UUID userId);

    void blockUser(UUID userId);
}
