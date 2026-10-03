package com.vn.auth.core.security;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@Setter
public class CustomUserDetails implements UserDetails {

    private UUID id;
    private UUID storeId;
    private String role;
    private String type;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String password;
    @Builder.Default
    private boolean active = true;
    private LocalDateTime createdAt;
    private List<String> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (role != null) {
            String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
            authorities.add(new SimpleGrantedAuthority(roleName));
        }

        if (roles != null) {
            roles.forEach(r -> {
                String roleName = r.startsWith("ROLE_") ? r : "ROLE_" + r.toUpperCase();
                authorities.add(new SimpleGrantedAuthority(roleName));
            });
        }

        return authorities;
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }
}
