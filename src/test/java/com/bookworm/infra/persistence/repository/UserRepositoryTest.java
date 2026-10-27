package com.bookworm.infra.persistence.repository;

import com.bookworm.BaseIntegrationTest;
import com.bookworm.infra.persistence.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserRepository JPA Integration Tests (PostgreSQL Testcontainers)")
@Transactional
class UserRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should persist and find user by case-insensitive email")
    void shouldPersistAndFindByEmailIgnoreCase() {
        String uniqueEmail = "user." + UUID.randomUUID() + "@example.com";

        UserEntity user = UserEntity.builder()
            .email(uniqueEmail)
            .passwordHash("$2a$12$e6n.fakeHashForTest")
            .firstName("Grace")
            .lastName("Hopper")
            .role(UserEntity.Role.CUSTOMER)
            .isActive(true)
            .build();

        UserEntity saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();

        Optional<UserEntity> found = userRepository.findByEmailIgnoreCase(uniqueEmail.toUpperCase());
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Grace");
        assertThat(found.get().getLastName()).isEqualTo("Hopper");
    }

    @Test
    @DisplayName("Should check email existence correctly")
    void shouldCheckExistsByEmailIgnoreCase() {
        String email = "ada.lovelace." + UUID.randomUUID() + "@example.com";

        UserEntity user = UserEntity.builder()
            .email(email)
            .passwordHash("$2a$12$e6n.fakeHashForTest")
            .firstName("Ada")
            .lastName("Lovelace")
            .role(UserEntity.Role.CUSTOMER)
            .isActive(true)
            .build();

        userRepository.save(user);

        boolean exists = userRepository.existsByEmailIgnoreCase(email.toUpperCase());
        assertThat(exists).isTrue();

        boolean notExists = userRepository.existsByEmailIgnoreCase("nonexistent@example.com");
        assertThat(notExists).isFalse();
    }
}
