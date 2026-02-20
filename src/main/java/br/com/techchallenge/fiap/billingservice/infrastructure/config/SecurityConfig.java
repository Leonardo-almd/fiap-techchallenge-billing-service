package br.com.techchallenge.fiap.billingservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration.
 *
 * When jwt.issuer-uri is empty/blank (e.g. local, e2e or current AWS deploy),
 * OAuth2 resource server is NOT configured and all requests are permitted.
 *
 * When jwt.issuer-uri is set to a real issuer URL, JWT validation is enabled
 * and only actuator + Swagger endpoints are public.
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
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    "/actuator/**",
                                    "/swagger-ui/**",
                                    "/swagger-ui.html",
                                    "/v3/api-docs/**",
                                    "/swagger-resources/**")
                            .permitAll()
                            .anyRequest()
                            .authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                    }));
        } else {
            // No OAuth2 provider — permit all requests
            http
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest()
                            .permitAll());
        }

        return http.build();
    }
}
