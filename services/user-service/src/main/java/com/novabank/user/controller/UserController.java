package com.novabank.user.controller;

import com.novabank.user.dto.request.*;
import com.novabank.user.dto.response.LoginResponse;
import com.novabank.user.dto.response.UserRegistrationResponse;
import com.novabank.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(
            @Valid @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserRegistrationResponse> getProfile() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal().toString();
        return ResponseEntity.ok(userService.getProfile(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserRegistrationResponse> updateProfile(
            @Valid @RequestBody UserProfileUpdateRequest request) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal().toString();
        return ResponseEntity.ok(userService.updateProfile(email, request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal().toString();
        String message = userService.changePassword(email, request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(userService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String message = userService.resetPassword(request);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/{userId}/activate")
    public ResponseEntity<String> activateUser(@PathVariable("userId") UUID userId) {
        userService.activateUser(userId);
        return ResponseEntity.ok("The user account has been activated.");
    }

    @PutMapping("/{userId}/block")
    public ResponseEntity<String> blockUser(@PathVariable("userId") UUID userId) {
        userService.blockUser(userId);
        return ResponseEntity.ok("The user account has been blocked.");
    }
}
