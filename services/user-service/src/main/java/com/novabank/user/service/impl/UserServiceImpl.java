package com.novabank.user.service.impl;

import com.novabank.user.dto.request.*;
import com.novabank.user.dto.response.LoginResponse;
import com.novabank.user.dto.response.UserRegistrationResponse;
import com.novabank.user.entity.PasswordResetToken;
import com.novabank.user.entity.User;
import com.novabank.user.enums.UserRole;
import com.novabank.user.enums.UserStatus;
import com.novabank.user.exception.*;
import com.novabank.user.mapper.UserMapper;
import com.novabank.user.repository.PasswordResetTokenRepository;
import com.novabank.user.repository.UserRepository;
import com.novabank.user.security.JwtService;
import com.novabank.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    @Transactional
    public UserRegistrationResponse register(UserRegistrationRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new UserAlreadyExistsException("Mobile number already exists");
        }

        User user = userMapper.toEntity(request);
        user.setStatus(UserStatus.PENDING);
        user.setRole(UserRole.CUSTOMER);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return LoginResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .status(user.getStatus())
                .role(user.getRole())
                .token(token)
                .build();
    }

    @Override
    public UserRegistrationResponse getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    @Override
    public UserRegistrationResponse updateProfile(String email, UserProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getFirstName().equals(request.getFirstName()) &&
                user.getLastName().equals(request.getLastName()) &&
                user.getMobileNumber().equals(request.getMobileNumber())) {
            throw new DuplicateRequestException("No changes detected.");
        }

        if (!user.getMobileNumber().equals(request.getMobileNumber())) {
            if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
                throw new DuplicateMobileNumberException(
                        "Mobile number already registered with another user.");
            }
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMobileNumber(request.getMobileNumber());
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    public String changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password and new password should not be the same.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidPasswordException("New password and confirm password do not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return "Password changed successfully.";
    }

    @Override
    public String forgotPassword(ForgotPasswordRequest request) {

        final String responseMessage =
                "If an account exists for this email, password reset instructions have been initiated.";

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return responseMessage;
        }

        User user = optionalUser.get();
        List<PasswordResetToken> existingTokens = passwordResetTokenRepository.findAllByUser(user);

        for (PasswordResetToken token : existingTokens) {
            token.setUsed(true);
        }
        passwordResetTokenRepository.saveAll(existingTokens);

        String resetToken = UUID.randomUUID().toString();

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(resetToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        passwordResetTokenRepository.save(passwordResetToken);

        log.info("Password reset token generated for user: {}", user.getEmail());
        log.debug("Password reset token: {}", resetToken);

        return responseMessage;
    }

    @Transactional
    @Override
    public String resetPassword(ResetPasswordRequest request) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidPasswordException("Invalid password reset token."));

        if (passwordResetToken.isUsed()) {
            throw new InvalidPasswordException("Password reset token has already been used.");
        }

        if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidPasswordException("Password reset token has expired.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidPasswordException("New password and confirm password do not match.");
        }

        User user = passwordResetToken.getUser();

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidPasswordException("New password must be different from the current password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetToken.setUsed(true);
        passwordResetTokenRepository.save(passwordResetToken);

        log.info("Log password reset successfully for user {}", user.getEmail());
        return "Password reset successfully.";
    }

    @Override
    @Transactional
    public void activateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new InvalidUserStatusException("User is already active.");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new InvalidUserStatusException("User is suspended. " +
                    "Suspended accounts cannot be activated directly.");
        }

        if (user.getStatus() == UserStatus.CLOSED) {
            throw new InvalidUserStatusException("User is closed. Closed account cannot be reopened");
        }

        if (user.getStatus().equals(UserStatus.PENDING) || user.getStatus().equals(UserStatus.BLOCKED)) {
            user.setStatus(UserStatus.ACTIVE);
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void blockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new InvalidUserStatusException("User is already blocked.");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new InvalidUserStatusException("Pending account cannot be blocked.");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new InvalidUserStatusException("Request is rejected. Suspended accounts cannot be blocked.");
        }

        if (user.getStatus() == UserStatus.CLOSED) {
            throw new InvalidUserStatusException("Request is rejected. Closed accounts cannot be blocked.");
        }

        if (user.getStatus().equals(UserStatus.ACTIVE)) {
            user.setStatus(UserStatus.BLOCKED);
        }

        userRepository.save(user);
    }
}
