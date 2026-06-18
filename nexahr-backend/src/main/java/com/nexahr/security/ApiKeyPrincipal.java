package com.nexahr.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class ApiKeyPrincipal implements UserDetails {

    private final Long apiKeyId;
    private final Long companyId;
    private final String keyName;
    private final Set<String> scopes;

    public ApiKeyPrincipal(Long apiKeyId, Long companyId, String keyName, String scopes) {
        this.apiKeyId = apiKeyId;
        this.companyId = companyId;
        this.keyName = keyName;
        this.scopes = Arrays.stream(scopes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public boolean hasScope(String scope) {
        return scopes.contains(scope);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_API"));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "api-key:" + apiKeyId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
