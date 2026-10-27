package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.AuthDTOs.AuthResponse;
import com.bookworm.api.v1.dto.AuthDTOs.LoginRequest;
import com.bookworm.api.v1.dto.AuthDTOs.RegisterRequest;
import com.bookworm.core.common.exception.DuplicateResourceException;
import com.bookworm.core.service.AuthService;
import com.bookworm.web.advice.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Web Unit Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("Should return 201 Created on valid registration payload")
        void shouldReturn201OnValidRegistration() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("john.doe@example.com");
            request.setPassword("StrongPass123!");
            request.setFirstName("John");
            request.setLastName("Doe");

            AuthResponse response = AuthResponse.builder()
                .accessToken("mock-jwt-access-token")
                .refreshToken("mock-jwt-refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .build();

            given(authService.register(any(RegisterRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(86400));
        }

        @Test
        @DisplayName("Should return 409 Conflict with RFC 9457 Problem Details when email is duplicated")
        void shouldReturn409OnDuplicateEmail() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("duplicate@example.com");
            request.setPassword("StrongPass123!");
            request.setFirstName("John");
            request.setLastName("Doe");

            given(authService.register(any(RegisterRequest.class)))
                .willThrow(new DuplicateResourceException("User with email duplicate@example.com already exists"));

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Duplicate Resource"))
                .andExpect(jsonPath("$.detail").value("User with email duplicate@example.com already exists"))
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("Should return 200 OK on successful credentials authentication")
        void shouldReturn200OnSuccessfulLogin() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("john.doe@example.com");
            request.setPassword("StrongPass123!");

            AuthResponse response = AuthResponse.builder()
                .accessToken("valid-token")
                .refreshToken("valid-refresh")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .build();

            given(authService.login(any(LoginRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("valid-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized with RFC 9457 Problem Details on bad credentials")
        void shouldReturn401OnBadCredentials() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("john.doe@example.com");
            request.setPassword("WrongPassword");

            given(authService.login(any(LoginRequest.class)))
                .willThrow(new BadCredentialsException("Invalid email or password"));

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.errorCode").value("BAD_CREDENTIALS"));
        }
    }
}
