package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.AuthDTOs;
import com.bookworm.core.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, token refresh, and session revocation.")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new customer account", operationId = "registerUser")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload or validation error"),
        @ApiResponse(responseCode = "409", description = "Email address already registered")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthDTOs.AuthTokenResponse> register(
        @Valid @RequestBody AuthDTOs.RegisterRequest request
    ) {
        log.info("REST request to register new user: {}", request.getEmail());
        AuthDTOs.AuthTokenResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Authenticate user credentials", operationId = "loginUser")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid login payload"),
        @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthDTOs.AuthTokenResponse> login(
        @Valid @RequestBody AuthDTOs.LoginRequest request
    ) {
        log.info("REST request to authenticate user: {}", request.getEmail());
        AuthDTOs.AuthTokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh expired access token", operationId = "refreshToken")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthDTOs.AuthTokenResponse> refresh(
        @Valid @RequestBody AuthDTOs.RefreshTokenRequest request
    ) {
        log.info("REST request to refresh access token");
        // Simulated token refresh
        AuthDTOs.AuthTokenResponse response = AuthDTOs.AuthTokenResponse.builder()
            .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." + java.util.UUID.randomUUID())
            .refreshToken(request.getRefreshToken())
            .tokenType("Bearer")
            .expiresIn(3600L)
            .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Revoke session and refresh token", operationId = "logoutUser")
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @Valid @RequestBody AuthDTOs.RefreshTokenRequest request
    ) {
        log.info("REST request to logout session");
        return ResponseEntity.noContent().build();
    }
}
