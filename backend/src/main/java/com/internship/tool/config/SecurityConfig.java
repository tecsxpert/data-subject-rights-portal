package com.internship.tool.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // Disable CSRF (needed for APIs)
            .csrf(csrf -> csrf.disable())

            // Authorization rules
            .authorizeHttpRequests(auth -> auth

                // ✅ Allow all frontend files
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/login.html",
                        "/register.html",
                        "/dashboard.html",
                        "/admin.html",
                        "/**/*.js",
                        "/**/*.css"
                ).permitAll()

                // ✅ Allow authentication APIs
                .requestMatchers("/api/auth/**").permitAll()

                // 🔒 Everything else requires authentication
                .anyRequest().authenticated()
            )

            // ✅ Stateless session (JWT)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ✅ Add JWT filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}