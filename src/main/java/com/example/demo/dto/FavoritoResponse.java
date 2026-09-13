package com.example.demo.dto;

import java.time.LocalDate;

//Este DTO es para una respuesta de Favorito, es decir, para enviar la informacion de un Favorito al cliente
public record FavoritoResponse(
    Long id,
    Long productoId,
    String nota,
    LocalDate fechaAlta
) {}
