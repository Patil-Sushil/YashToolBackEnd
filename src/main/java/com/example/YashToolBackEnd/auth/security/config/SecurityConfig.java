// src/main/java/com/kalibyte/foundry/auth/security/config/SecurityConfig.java
package com.example.YashToolBackEnd.auth.security.config;


import com.example.YashToolBackEnd.auth.security.filter.JwtAuthenticationFilter;
import com.example.YashToolBackEnd.auth.security.handler.JwtAccessDeniedHandler;
import com.example.YashToolBackEnd.auth.security.handler.JwtAuthenticationEntryPoint;
import com.example.YashToolBackEnd.auth.security.token.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
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

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/webjars/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // ========================================
                        // GST REPORTS - CA ROLE (EXCLUSIVE ACCESS)
                        // ========================================
                        .requestMatchers("/api/gst/**")
                        .hasAnyRole("CA", "ADMIN")

                        .requestMatchers("/actuator/**").permitAll()

                        // Reports
                        .requestMatchers("/api/reports/**")
                        .hasAnyRole("ADMIN", "MANAGER", "FINANCE")

                        // Admin
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Sales
                        .requestMatchers("/api/enquiries/**", "/api/quotation/**")
                        .hasAnyRole("ADMIN", "SALES")

                        // Customers: only ADMIN and SALES can create/update, read allowed to authenticated users
                        .requestMatchers(HttpMethod.POST, "/api/customers/**").hasAnyRole("ADMIN", "SALES")
                        .requestMatchers(HttpMethod.PATCH, "/api/customers/**").hasAnyRole("ADMIN", "SALES")
                        .requestMatchers(HttpMethod.GET, "/api/customers/**").authenticated()

                        // Masters: write restricted to ADMIN/STORE, read allowed to authenticated users
                        .requestMatchers(HttpMethod.POST, "/api/masters/**").hasAnyRole("ADMIN", "STORE")
                        .requestMatchers(HttpMethod.DELETE, "/api/masters/**").hasAnyRole("ADMIN", "STORE")
                        .requestMatchers(HttpMethod.GET, "/api/masters/**").authenticated()

                        // Production
                        .requestMatchers("/api/production/**")
                        .hasAnyRole("ADMIN", "PRODUCTION")

                        // Finance
                        .requestMatchers("/api/finance/**")
                        .hasAnyRole("ADMIN", "FINANCE")

                        // Inventory
                        .requestMatchers(
                                "/api/items/**",
                                "/api/vendors/**",
                                "/api/departments/**",
                                "/api/purchase-orders/**",
                                "/api/inwards/**",
                                "/api/material-issues/**",
                                "/api/inventory/reports/**"
                        ).hasAnyRole("ADMIN", "STORE", "FINANCE")

                        // QA
                        .requestMatchers("/api/qa/**")
                        .hasAnyRole("ADMIN", "QUALITY", "PRODUCTION")

                        // Labor Management
                        .requestMatchers(
                                "/api/labors/**",
                                "/api/attendance/**",
                                "/api/advances/**",
                                "/api/labor-reports/**",
                                "/api/payouts/**"
                        ).hasRole("ADMIN")

                        // Scrap Management
                        .requestMatchers("/api/scrap/**")
                        .hasAnyRole("ADMIN", "PRODUCTION", "QUALITY", "METALLURGIST")

                        // Furnace Management
                        .requestMatchers("/api/furnace/**", "/api/electricity-rate/**")
                        .hasAnyRole("ADMIN", "PRODUCTION")

                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}