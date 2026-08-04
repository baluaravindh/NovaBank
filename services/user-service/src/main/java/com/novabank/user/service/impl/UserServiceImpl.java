package com.novabank.user.service.impl;

import com.novabank.user.dto.request.LoginRequest;
import com.novabank.user.dto.request.UserProfileUpdateRequest;
import com.novabank.user.dto.request.UserRegistrationRequest;
import com.novabank.user.dto.response.LoginResponse;
import com.novabank.user.dto.response.UserRegistrationResponse;
import com.novabank.user.entity.User;
import com.novabank.user.enums.UserStatus;
import com.novabank.user.exception.*;
import com.novabank.user.mapper.UserMapper;
import com.novabank.user.repository.UserRepository;
import com.novabank.user.security.JwtService;
import com.novabank.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

        String token = jwtService.generateToken(user.getEmail());

        return LoginResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .status(user.getStatus())
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
}
