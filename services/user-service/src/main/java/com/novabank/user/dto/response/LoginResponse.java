package com.novabank.user.dto.response;

import com.novabank.user.enums.UserRole;
import com.novabank.user.enums.UserStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private UserStatus status;
    private UserRole role;
    private String token;
}
