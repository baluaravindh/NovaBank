package com.novabank.user.mapper;

import com.novabank.user.dto.request.UserRegistrationRequest;
import com.novabank.user.dto.response.UserRegistrationResponse;
import com.novabank.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRegistrationRequest request);

    UserRegistrationResponse toResponse(User user);
}
