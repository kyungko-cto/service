package com.delivery.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * JWT Claim 기반 사용자 Principal
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    private final Long id;       // 사용자 PK
    private final String email;  // 사용자 이메일
    private final String role;   // 권한 (ROLE_USER, ROLE_ADMIN 등)
    private final String grade;  // 사용자 등급 (BRONZE, SILVER 등)

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> role);
    }

    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
