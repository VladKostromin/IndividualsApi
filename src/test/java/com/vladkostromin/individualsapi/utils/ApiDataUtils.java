package com.vladkostromin.individualsapi.utils;

import com.vladkostromin.individualsapi.model.*;
import com.vladkostrov.dto.rest.userserviceapi.RegisterIndividualRequest;
import com.vladkostrov.dto.rest.userserviceapi.RegisterIndividualResponse;
import com.vladkostrov.dto.userservice.AddressDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

public class ApiDataUtils {

    private static final String userId = UUID.randomUUID().toString();

    public static TokenResponse getTokenResponse() {
        return TokenResponse.builder()
                .accessToken("test access token")
                .refreshToken("test refresh token")
                .expiresIn(1730953787)
                .tokenType("Bearer")
                .build();
    }

    public static UserResponse getUser() {
        return UserResponse.builder()
                .id(userId)
                .roles(Collections.emptyList())
                .email("testuser@mail.com")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static RegisterIndividualResponse getRegisterIndividualResponse() {
        return RegisterIndividualResponse.builder()
                .userId(UUID.randomUUID())
                .status("CREATED")
                .build();
    }

    public static RefreshTokenRequest getRefreshTokenRequest() {
        return RefreshTokenRequest.builder()
                .refreshToken("test refresh token")
                .build();
    }

    public static AuthRequest getAuthRequest() {
        return AuthRequest.builder()
                .email("testuser@mail.com")
                .password("testPassword")
                .build();
    }
    public static UserRegistrationRequest getUserRegistrationRequest() {
        return UserRegistrationRequest.builder()
                .email("testuser@mail.com")
                .password("testPassword")
                .confirmPassword("testPassword")
                .registerInfo(RegisterIndividualRequest.builder()
                        .email("testuser@mail.com")
                        .firstName("testName")
                        .lastName("testLastName")
                        .phoneNumber("testPhoneNumber")
                        .passportNumber("testPassportNumber")
                        .address(AddressDto.builder()
                                .address("testAddress")
                                .city("testCity")
                                .countryName("testCountryName")
                                .countryCode("testCountryCode")
                                .zipCode("testZipCode")
                                .build())
                        .build())
                .build();
    }

}
