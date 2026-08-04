package com.novabank.user.mapper;

import com.novabank.user.dto.request.UserRegistrationRequest;
import com.novabank.user.dto.response.UserRegistrationResponse;
import com.novabank.user.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-04T01:49:03+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.1 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(UserRegistrationRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.firstName( request.getFirstName() );
        user.lastName( request.getLastName() );
        user.email( request.getEmail() );
        user.mobileNumber( request.getMobileNumber() );
        user.password( request.getPassword() );
        user.dateOfBirth( request.getDateOfBirth() );

        return user.build();
    }

    @Override
    public UserRegistrationResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserRegistrationResponse.UserRegistrationResponseBuilder userRegistrationResponse = UserRegistrationResponse.builder();

        userRegistrationResponse.id( user.getId() );
        userRegistrationResponse.firstName( user.getFirstName() );
        userRegistrationResponse.lastName( user.getLastName() );
        userRegistrationResponse.email( user.getEmail() );
        userRegistrationResponse.mobileNumber( user.getMobileNumber() );
        userRegistrationResponse.status( user.getStatus() );
        userRegistrationResponse.createdAt( user.getCreatedAt() );

        return userRegistrationResponse.build();
    }
}
