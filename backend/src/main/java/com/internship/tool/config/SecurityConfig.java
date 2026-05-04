package com.internship.tool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                // ✅ allow frontend pages
                .requestMatchers(
                        "/login.html",
                        "/register.html",
                        "/admin.html",
                        "/dashboard.html",
                        "/favicon.ico"
                ).permitAll()

                // ✅ allow auth APIs
                .requestMatchers("/api/auth/**").permitAll()

                // ✅ allow requests API (THIS FIXES YOUR ERROR)
                .requestMatchers("/api/requests/**").permitAll()

                // everything else
                .anyRequest().authenticated()
            );

        return http.build();
    }
}