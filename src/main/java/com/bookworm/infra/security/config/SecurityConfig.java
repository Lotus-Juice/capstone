package com.bookworm.infra.security.config;

import com.bookworm.infra.security.jwt.CustomAccessDeniedHandler;
import com.bookworm.infra.security.jwt.JwtAuthenticationEntryPoint;
import com.bookworm.infra.security.jwt.JwtAuthenticationFilter;
import com.bookworm.infra.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedHandler)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public & Discovery Endpoints (Permit All)
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health"
                ).permitAll()

                // Public Catalog & Search Endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/authors/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/publishers/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/search/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/shipping/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/coupons/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/recommendations/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/coupons/validate").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/payments/webhook").permitAll()

                // Shopping Cart (GUEST or CUSTOMER)
                .requestMatchers("/api/v1/cart/**").hasAnyRole("GUEST", "CUSTOMER", "ADMIN")

                // Customer Protected Endpoints (CUSTOMER or ADMIN)
                .requestMatchers("/api/v1/users/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/v1/wishlist/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/v1/orders/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/v1/payments/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/books/*/reviews").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/authors/*/follow").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/authors/*/follow").hasAnyRole("CUSTOMER", "ADMIN")

                // Admin Endpoints
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // Any other request must be authenticated
                .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://bookworm.example.com", "http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Guest-Session-Token", "Idempotency-Key"));
        configuration.setExposedHeaders(List.of("Authorization", "Retry-After"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
