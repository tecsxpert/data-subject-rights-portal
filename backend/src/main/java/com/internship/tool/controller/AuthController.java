package com.internship.tool.controller;

import com.internship.tool.entity.User;
import com.internship.tool.repository.UserRepository;
import com.internship.tool.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository repo;

    @Autowired
    private JwtUtil jwtUtil;

    // =========================
    // REGISTER USER (JSON)
    // =========================
    @GetMapping("/register")
public String quickRegister() {
    User user = new User();
    user.setUsername("admin");
    user.setPassword("admin123");
    repo.save(user);
    return "User registered";
}
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return repo.save(user);
    }

    // =========================
    // LOGIN (JSON - Postman / curl)
    // =========================
    @PostMapping(value = "/login", consumes = "application/json")
    public Map<String, String> loginJson(@RequestBody User user) {

        Optional<User> existingUser = repo.findByUsername(user.getUsername());

        if (existingUser.isPresent() &&
                existingUser.get().getPassword().equals(user.getPassword())) {

            String token = jwtUtil.generateToken(user.getUsername());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);

            return response;
        }

        throw new RuntimeException("Invalid Username or Password");
    }

    // =========================
    // LOGIN (Browser - EASY MODE)
    // =========================
    @GetMapping("/login")
    public String loginBrowser(@RequestParam String username,
                              @RequestParam String password) {

        Optional<User> existingUser = repo.findByUsername(username);

        if (existingUser.isPresent() &&
                existingUser.get().getPassword().equals(password)) {

            return jwtUtil.generateToken(username);
        }

        throw new RuntimeException("Invalid Username or Password");
    }
}