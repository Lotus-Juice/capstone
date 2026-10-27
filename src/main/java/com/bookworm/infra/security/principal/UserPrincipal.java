package com.bookworm.infra.security.principal;

import com.bookworm.infra.persistence.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final String role;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(UserEntity user) {
        String roleWithPrefix = user.getRole().startsWith("ROLE_") ? user.getRole() : "ROLE_" + user.getRole();
        GrantedAuthority authority = new SimpleGrantedAuthority(roleWithPrefix);

        return UserPrincipal.builder()
            .id(user.getId())
            .email(user.getEmail())
            .password(user.getPasswordHash())
            .role(user.getRole())
            .active(user.isActive())
            .authorities(Collections.singletonList(authority))
            .build();
    }

    public static UserPrincipal createGuest(String guestToken) {
        return UserPrincipal.builder()
            .id(null)
            .email("guest@" + guestToken)
            .password("")
            .role("GUEST")
            .active(true)
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_GUEST")))
            .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
