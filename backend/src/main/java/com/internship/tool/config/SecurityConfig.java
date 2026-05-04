package com.internship.tool.config;

import com.internship.tool.config.JwtAuthFilter;
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
            // ❌ Disable CSRF (important for APIs)
            .csrf(csrf -> csrf.disable())

            // ✅ Authorization rules
            .authorizeHttpRequests(auth -> auth

                // ✅ Allow frontend pages
                .requestMatchers(
                        "/",
                        "/login.html",
                        "/register.html",
                        "/dashboard.html",
                        "/script.js",
                        "/style.css"
                ).permitAll()

                // ✅ Allow auth APIs
                .requestMatchers("/api/auth/**").permitAll()

                // 🔒 Everything else requires JWT
                .anyRequest().authenticated()
            )

            // ✅ Add JWT filter (VERY IMPORTANT)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // ❌ No session (JWT only)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }
}