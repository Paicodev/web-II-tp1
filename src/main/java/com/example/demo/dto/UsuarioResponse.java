package com.example.demo.dto;

import com.example.demo.model.Rol;

public record UsuarioResponse(
    Long id,
    String email,
    Rol rol
) {}
