package com.bookworm.core.service;

import com.bookworm.api.v1.dto.AddressDTOs;
import com.bookworm.api.v1.dto.AuthDTOs;
import com.bookworm.core.common.exception.BusinessException;
import com.bookworm.core.common.exception.DuplicateResourceException;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.UserMapper;
import com.bookworm.infra.persistence.entity.UserAddressEntity;
import com.bookworm.infra.persistence.entity.UserEntity;
import com.bookworm.infra.persistence.repository.UserAddressRepository;
import com.bookworm.infra.persistence.repository.UserRepository;
import com.bookworm.infra.security.jwt.JwtProvider;
import com.bookworm.infra.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthDTOs.AuthTokenResponse register(AuthDTOs.RegisterRequest request) {
        log.info("Processing registration request for email: {}", request.getEmail());

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            log.warn("Registration failed. Email already exists: {}", request.getEmail());
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        UserEntity user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("CUSTOMER");
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);
        log.info("Successfully registered user with ID: {}", savedUser.getId());

        return generateTokenResponse(savedUser);
    }

    @Transactional
    public AuthDTOs.AuthTokenResponse login(AuthDTOs.LoginRequest request) {
        log.info("Processing login attempt for email: {}", request.getEmail());

        UserEntity user = userRepository.findByEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Invalid email or password"));

        if (!user.isActive()) {
            log.warn("Login rejected for deactivated account ID: {}", user.getId());
            throw new BusinessException("ACCOUNT_DEACTIVATED", "Account has been deactivated. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Password mismatch for email: {}", request.getEmail());
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid email or password");
        }

        userRepository.updateLastLoginAt(user.getId(), Instant.now());
        log.info("User ID: {} successfully authenticated", user.getId());

        return generateTokenResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthDTOs.UserProfileResponse getUserProfile(UUID userId) {
        UserEntity user = findUserOrThrow(userId);
        return userMapper.toProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public List<AddressDTOs.AddressDTO> getUserAddresses(UUID userId) {
        List<UserAddressEntity> addresses = userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        return userMapper.toAddressDTOList(addresses);
    }

    @Transactional
    public AddressDTOs.AddressDTO addUserAddress(UUID userId, AddressDTOs.CreateAddressRequest request) {
        log.info("Adding new address for user ID: {}", userId);
        UserEntity user = findUserOrThrow(userId);

        long addressCount = userAddressRepository.countByUserId(userId);
        if (addressCount >= 10) {
            throw new BusinessException("MAX_ADDRESS_LIMIT", "Maximum limit of 10 delivery addresses reached");
        }

        if (request.isDefault() || addressCount == 0) {
            userAddressRepository.resetDefaultAddressForUser(userId);
        }

        UserAddressEntity address = userMapper.toAddressEntity(request);
        address.setUser(user);
        if (addressCount == 0) {
            address.setDefault(true);
        }

        UserAddressEntity savedAddress = userAddressRepository.save(address);
        log.info("Saved delivery address ID: {} for user ID: {}", savedAddress.getId(), userId);
        return userMapper.toAddressDTO(savedAddress);
    }

    @Transactional
    public void deleteUserAddress(UUID userId, UUID addressId) {
        log.info("Deleting address ID: {} for user ID: {}", addressId, userId);
        UserAddressEntity address = userAddressRepository.findByIdAndUserId(addressId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        address.setDeleted(true);
        userAddressRepository.save(address);
    }

    private UserEntity findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private AuthDTOs.AuthTokenResponse generateTokenResponse(UserEntity user) {
        UserPrincipal principal = UserPrincipal.create(user);
        String accessToken = jwtProvider.generateAccessToken(principal);
        String refreshToken = jwtProvider.generateRefreshToken(principal);

        return AuthDTOs.AuthTokenResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole())
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(3600L)
            .build();
    }
}
