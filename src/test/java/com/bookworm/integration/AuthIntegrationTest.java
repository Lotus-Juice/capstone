package com.bookworm.integration;

import com.bookworm.BaseIntegrationTest;
import com.bookworm.api.v1.dto.AuthDTOs.AuthResponse;
import com.bookworm.api.v1.dto.AuthDTOs.LoginRequest;
import com.bookworm.api.v1.dto.AuthDTOs.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Auth Integration Test (PostgreSQL Testcontainers)")
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Complete Auth Flow: Register -> Login -> Profile Access")
    void completeAuthenticationFlow() {
        // 1. Register new customer
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("integration.user@example.com");
        registerReq.setPassword("P@ssword123!");
        registerReq.setFirstName("Enterprise");
        registerReq.setLastName("Tester");

        ResponseEntity<AuthResponse> registerRes = restTemplate.postForEntity(
            "/api/v1/auth/register",
            registerReq,
            AuthResponse.class
        );

        assertThat(registerRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerRes.getBody()).isNotNull();
        assertThat(registerRes.getBody().getAccessToken()).isNotBlank();

        // 2. Login with registered credentials
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("integration.user@example.com");
        loginReq.setPassword("P@ssword123!");

        ResponseEntity<AuthResponse> loginRes = restTemplate.postForEntity(
            "/api/v1/auth/login",
            loginReq,
            AuthResponse.class
        );

        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginRes.getBody()).isNotNull();
        String jwtToken = loginRes.getBody().getAccessToken();
        assertThat(jwtToken).isNotBlank();

        // 3. Access Protected User Profile Endpoint with Bearer Token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> profileRes = restTemplate.exchange(
            "/api/v1/users/profile",
            HttpMethod.GET,
            entity,
            String.class
        );

        assertThat(profileRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(profileRes.getBody()).contains("integration.user@example.com");
    }
}
