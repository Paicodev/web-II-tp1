package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Este DTO es para solicitar la creacion de un Favorito, es decir, para recibir la informacion de un Favorito desde el cliente
public record CrearFavoritoRequest (
    @NotNull Long productoId,
    @NotBlank @Size(max = 200) String nota
){}