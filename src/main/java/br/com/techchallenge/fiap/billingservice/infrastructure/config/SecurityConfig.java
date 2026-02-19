package br.com.techchallenge.fiap.billingservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration. Permits actuator health endpoints without
 * authentication
 * so Kubernetes liveness/readiness/startup probes succeed (they do not send
 * JWT).
 *
 * When jwt.issuer-uri is empty/blank (e.g. local or e2e environments),
 * OAuth2 resource server is NOT configured and all requests are permitted.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String jwtIssuerUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (jwtIssuerUri != null && !jwtIssuerUri.isBlank()) {
            // Production / staging: protect endpoints with JWT
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(request -> request.getRequestURI() != null
                                    && request.getRequestURI().contains("/actuator/"))
                            .permitAll()
                            .anyRequest()
                            .authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                    }));
        } else {
            // Local / e2e: no OAuth2 provider — permit all requests
            http
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest()
                            .permitAll());
        }

        return http.build();
    }
}
