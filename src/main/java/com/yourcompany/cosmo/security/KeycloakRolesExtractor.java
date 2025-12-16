package com.yourcompany.cosmo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public final class KeycloakRolesExtractor {
  private KeycloakRolesExtractor() {}

  public static Collection<GrantedAuthority> extract(Jwt jwt) {
    Set<String> roles = new HashSet<>();

    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess != null) {
      Object rs = realmAccess.get("roles");
      if (rs instanceof Collection<?> col) {
        col.forEach(r -> roles.add(String.valueOf(r)));
      }
    }

    Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
    if (resourceAccess != null) {
      resourceAccess.values().forEach(v -> {
        if (v instanceof Map<?, ?> m) {
          Object rs = m.get("roles");
          if (rs instanceof Collection<?> col) {
            col.forEach(r -> roles.add(String.valueOf(r)));
          }
        }
      });
    }

    return roles.stream()
        .map(r -> "ROLE_" + r.toUpperCase(Locale.ROOT))
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toUnmodifiableSet());
  }
}
