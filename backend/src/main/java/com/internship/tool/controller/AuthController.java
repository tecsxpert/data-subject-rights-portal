package com.internship.tool.controller;

import com.internship.tool.config.JwtUtil;
import com.internship.tool.config.UserPrincipal;
import com.internship.tool.entity.User;
import com.internship.tool.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, register, and token refresh")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil               jwtUtil;
    private final UserRepository        userRepo;
    private final PasswordEncoder       encoder;

    // ── LOGIN ─────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive a JWT token")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest req) {

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User user = userRepo.findByUsername(principal.getUsername()).orElseThrow();
        String token = jwtUtil.generate(user);

        return ResponseEntity.ok(Map.of(
                "token",    token,
                "username", user.getUsername(),
                "role",     user.getRole()
        ));
    }

    // ── REGISTER ──────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Register a new USER-role account")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest req) {

        if (userRepo.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username already taken"));
        }
        if (userRepo.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email already registered"));
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .role("USER")
                .active(true)
                .build();
        userRepo.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully"));
    }

    // ── REFRESH ───────────────────────────────────────────────

    @PostMapping("/refresh")
    @Operation(summary = "Issue a fresh token for the authenticated user")
    public ResponseEntity<Map<String, Object>> refresh(
            @AuthenticationPrincipal UserDetails principal) {

        User user = userRepo.findByUsername(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "token",    jwtUtil.generate(user),
                "username", user.getUsername(),
                "role",     user.getRole()
        ));
    }

    // ── Inner request DTOs ────────────────────────────────────

    @Data
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank @Size(min = 3, max = 50)  private String username;
        @NotBlank @Email                    private String email;
        @NotBlank @Size(min = 8, max = 100) private String password;
    }
}
