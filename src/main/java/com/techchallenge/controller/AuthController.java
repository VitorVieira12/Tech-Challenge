package com.techchallenge.controller;

import com.techchallenge.domain.dto.LoginRequestDTO;
import com.techchallenge.domain.dto.LoginResponseDTO;
import com.techchallenge.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticação.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints de autenticação")
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint de login.
     * Autentica um usuário e retorna um token JWT.
     * 
     * POST /api/auth/login
     * 
     * Credenciais padrão (MVP):
     * - Username: admin
     * - Password: admin
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica um usuário e retorna um token JWT")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}



