package com.bookworm.integration;

import com.bookworm.BaseIntegrationTest;
import com.bookworm.api.v1.dto.AuthDTOs.AuthResponse;
import com.bookworm.api.v1.dto.AuthDTOs.RegisterRequest;
import com.bookworm.infra.persistence.entity.UserEntity;
import com.bookworm.infra.persistence.repository.UserRepository;
import com.bookworm.infra.security.jwt.JwtProvider;
import com.bookworm.infra.security.principal.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Security & RBAC Integration Tests (PostgreSQL Testcontainers)")
class SecurityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private String customerJwtToken;
    private String adminJwtToken;

    @BeforeEach
    void setUpSecurityUsers() {
        // Register customer
        String customerEmail = "sec.customer." + UUID.randomUUID() + "@example.com";
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail(customerEmail);
        registerReq.setPassword("CustomerPass123!");
        registerReq.setFirstName("SecCustomer");
        registerReq.setLastName("User");

        ResponseEntity<AuthResponse> regRes = restTemplate.postForEntity(
            "/api/v1/auth/register",
            registerReq,
            AuthResponse.class
        );
        customerJwtToken = regRes.getBody().getAccessToken();

        // Create Admin User directly and generate Admin JWT
        UserEntity adminUser = UserEntity.builder()
            .email("sec.admin." + UUID.randomUUID() + "@example.com")
            .passwordHash("$2a$12$e6n.fakeAdminHash")
            .firstName("Super")
            .lastName("Admin")
            .role(UserEntity.Role.ADMIN)
            .isActive(true)
            .build();
        adminUser = userRepository.save(adminUser);

        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);
        adminJwtToken = jwtProvider.generateAccessToken(adminPrincipal);
    }

    @Nested
    @DisplayName("Public Access Tests")
    class PublicAccessTests {

        @Test
        @DisplayName("Catalog endpoints must be accessible anonymously without Authorization header")
        void publicCatalogEndpointsAccessibleAnonymously() {
            ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/books", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Categories endpoints must be accessible anonymously")
        void publicCategoriesEndpointsAccessibleAnonymously() {
            ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/categories", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("Authentication & Unauthorized (401) Tests")
    class UnauthorizedAccessTests {

        @Test
        @DisplayName("Should return 401 Unauthorized Problem Details when accessing protected endpoint without JWT")
        void protectedEndpointWithoutJwtReturns401() {
            ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/users/profile", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
            assertThat(response.getBody()).contains("Full authentication is required");
            assertThat(response.getBody()).contains("UNAUTHORIZED");
        }

        @Test
        @DisplayName("Should return 401 Unauthorized Problem Details when JWT token is invalid or corrupted")
        void corruptedJwtTokenReturns401() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("invalid.corrupted.jwtTokenPayload12345");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users/profile",
                HttpMethod.GET,
                entity,
                String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        }
    }

    @Nested
    @DisplayName("Role-Based Access Control & Forbidden (403) Tests")
    class RbacForbiddenTests {

        @Test
        @DisplayName("Should return 403 Forbidden with RFC 9457 Problem Details when CUSTOMER accesses ADMIN endpoint")
        void customerAccessingAdminEndpointReturns403() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(customerJwtToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/dashboard",
                HttpMethod.GET,
                entity,
                String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
            assertThat(response.getBody()).contains("ACCESS_DENIED");
            assertThat(response.getBody()).contains("Access Denied");
        }
    }

    @Nested
    @DisplayName("Cart Session Header Security Tests")
    class CartSecurityTests {

        @Test
        @DisplayName("Cart endpoint must allow access with X-Guest-Session-Token header")
        void cartAccessibleWithGuestSessionHeader() {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Guest-Session-Token", "guest-uuid-token-555");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/cart",
                HttpMethod.GET,
                entity,
                String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Cart endpoint must allow access with Customer JWT Bearer token")
        void cartAccessibleWithCustomerBearerToken() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(customerJwtToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/cart",
                HttpMethod.GET,
                entity,
                String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
