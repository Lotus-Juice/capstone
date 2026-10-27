package com.bookworm.core.service;

import com.bookworm.api.v1.dto.AuthDTOs.AuthResponse;
import com.bookworm.api.v1.dto.AuthDTOs.LoginRequest;
import com.bookworm.api.v1.dto.AuthDTOs.RefreshTokenRequest;
import com.bookworm.api.v1.dto.AuthDTOs.RegisterRequest;
import com.bookworm.core.common.exception.DuplicateResourceException;
import com.bookworm.core.common.exception.InvalidTokenException;
import com.bookworm.core.mapper.UserMapper;
import com.bookworm.infra.persistence.entity.UserEntity;
import com.bookworm.infra.persistence.repository.UserRepository;
import com.bookworm.infra.security.jwt.JwtProvider;
import com.bookworm.infra.security.principal.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private UserEntity sampleUser;
    private UserPrincipal samplePrincipal;

    @BeforeEach
    void setUp() {
        sampleUser = UserEntity.builder()
            .id(UUID.randomUUID())
            .email("alice@example.com")
            .passwordHash("$2a$12$hashedPasswordExample")
            .firstName("Alice")
            .lastName("Smith")
            .role(UserEntity.Role.CUSTOMER)
            .isActive(true)
            .build();

        samplePrincipal = UserPrincipal.create(sampleUser);
    }

    @Nested
    @DisplayName("User Registration")
    class RegisterTests {

        @Test
        @DisplayName("Should successfully register a new user and generate JWT tokens")
        void shouldRegisterNewUserSuccessfully() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("alice@example.com");
            request.setPassword("SecurePass123!");
            request.setFirstName("Alice");
            request.setLastName("Smith");

            given(userRepository.existsByEmailIgnoreCase("alice@example.com")).willReturn(false);
            given(passwordEncoder.encode(request.getPassword())).willReturn("$2a$12$hashedPasswordExample");
            given(userRepository.save(any(UserEntity.class))).willReturn(sampleUser);
            given(jwtProvider.generateAccessToken(any(UserPrincipal.class))).willReturn("mock-access-token");
            given(jwtProvider.generateRefreshToken(any(UserPrincipal.class))).willReturn("mock-refresh-token");
            given(jwtProvider.getAccessTokenExpirationSeconds()).willReturn(86400L);

            AuthResponse response = authService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(86400L);
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("alice@example.com");
            request.setPassword("SecurePass123!");
            request.setFirstName("Alice");
            request.setLastName("Smith");

            given(userRepository.existsByEmailIgnoreCase("alice@example.com")).willReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("User with email alice@example.com already exists");
        }
    }

    @Nested
    @DisplayName("User Login")
    class LoginTests {

        @Test
        @DisplayName("Should authenticate valid credentials and return JWT tokens")
        void shouldLoginSuccessfully() {
            LoginRequest request = new LoginRequest();
            request.setEmail("alice@example.com");
            request.setPassword("SecurePass123!");

            Authentication authentication = new UsernamePasswordAuthenticationToken(samplePrincipal, null, samplePrincipal.getAuthorities());
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authentication);
            given(jwtProvider.generateAccessToken(samplePrincipal)).willReturn("mock-access-token");
            given(jwtProvider.generateRefreshToken(samplePrincipal)).willReturn("mock-refresh-token");
            given(jwtProvider.getAccessTokenExpirationSeconds()).willReturn(86400L);

            AuthResponse response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getEmail()).isEqualTo("alice@example.com");
        }

        @Test
        @DisplayName("Should propagate BadCredentialsException on invalid password")
        void shouldThrowBadCredentialsOnInvalidPassword() {
            LoginRequest request = new LoginRequest();
            request.setEmail("alice@example.com");
            request.setPassword("WrongPassword!");

            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("Token Refresh")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should issue new token pair on valid refresh token")
        void shouldRefreshTokenSuccessfully() {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("valid-refresh-token");

            UUID userId = sampleUser.getId();
            given(jwtProvider.validateToken("valid-refresh-token")).willReturn(true);
            given(jwtProvider.getUserIdFromToken("valid-refresh-token")).willReturn(userId);
            given(userRepository.findById(userId)).willReturn(Optional.of(sampleUser));
            given(jwtProvider.generateAccessToken(any(UserPrincipal.class))).willReturn("new-access-token");
            given(jwtProvider.generateRefreshToken(any(UserPrincipal.class))).willReturn("new-refresh-token");
            given(jwtProvider.getAccessTokenExpirationSeconds()).willReturn(86400L);

            AuthResponse response = authService.refreshToken(request);

            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        }

        @Test
        @DisplayName("Should throw InvalidTokenException when refresh token is invalid or expired")
        void shouldThrowExceptionOnInvalidRefreshToken() {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("invalid-or-expired-token");

            given(jwtProvider.validateToken("invalid-or-expired-token")).willReturn(false);

            assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid or expired refresh token");
        }
    }
}
