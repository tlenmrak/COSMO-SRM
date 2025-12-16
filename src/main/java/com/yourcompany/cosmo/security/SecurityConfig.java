package com.yourcompany.cosmo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/webjars/**", "/swagger-resources/**").permitAll()

                        .pathMatchers("/api/raw-materials/**").hasAnyRole("DIRECTOR", "TECHNOLOGIST")
                        .pathMatchers("/api/recipes/**", "/api/products/**", "/api/batch-templates/**", "/api/batches/**")
                        .hasAnyRole("DIRECTOR", "TECHNOLOGIST")

                        .pathMatchers("/api/**").hasAnyRole("DIRECTOR", "TECHNOLOGIST")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                )
                .build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthConverter() {
        return jwt -> Mono.just(new JwtAuthenticationToken(jwt, KeycloakRolesExtractor.extract(jwt)));
    }
}