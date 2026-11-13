package com.vms.backend.controller;

import com.vms.backend.dto.ErrorResponse;
import com.vms.backend.dto.LoginRequest;
import com.vms.backend.dto.LoginResponse;
import com.vms.backend.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final String adminEmail;
    private final String encodedPassword;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            @Value("${vms.auth.username}") String adminEmail,
            @Value("${vms.auth.password}") String encodedPassword,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.adminEmail = adminEmail;
        this.encodedPassword = encodedPassword;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        boolean emailMatch = adminEmail.equals(request.email());
        boolean passwordMatch = emailMatch && passwordEncoder.matches(request.password(), encodedPassword);

        if (!emailMatch || !passwordMatch) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    "Invalid credentials",
                    httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        String token = jwtService.generateToken(adminEmail);
        return ResponseEntity.ok(new LoginResponse(token, adminEmail));
    }
}
