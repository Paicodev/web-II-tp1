package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearListaRequest(
    @NotBlank @Size(max = 100) String nombre
) {}
