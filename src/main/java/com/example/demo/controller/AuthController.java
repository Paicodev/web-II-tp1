package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.RegistroRequest;
import com.example.demo.dto.UsuarioResponse;
import com.example.demo.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints de registro e inicio de sesión")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario con rol USER por defecto y contraseña encriptada")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        UsuarioResponse creado = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

        @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida credenciales contra la base de datos y devuelve un token JWT firmado")
    public ResponseEntity<com.example.demo.dto.LoginResponse> login(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.example.demo.dto.LoginRequest request) {
        com.example.demo.dto.LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

}
