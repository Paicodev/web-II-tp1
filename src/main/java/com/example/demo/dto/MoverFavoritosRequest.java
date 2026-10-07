package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public record MoverFavoritosRequest(
        @NotNull
        Long destinoId
        ) {}
