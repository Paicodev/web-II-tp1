package com.example.demo.model;

import java.time.LocalDate;

//Este es un molde de datos que representa un producto en la app
public record Favorito(
    Long id,
    Long productoId,
    String nota, 
    LocalDate fecha
) {}
