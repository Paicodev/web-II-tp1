package com.example.demo.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CrearListaRequest;
import com.example.demo.dto.FavoritoResponse;
import com.example.demo.dto.ListaResponse;
import com.example.demo.service.ListaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/listas")
@Tag(name = "Listas", description = "Operaciones sobre las listas de favoritos")
public class ListaController {

    private final ListaService service;

    public ListaController(ListaService service) {
        this.service = service;
    }

    // Crear lista (201 Created)
    @PostMapping
    @Operation(summary = "Crear lista", description = "Crea una nueva lista para organizar favoritos")
    public ResponseEntity<ListaResponse> crear(@Valid @RequestBody CrearListaRequest request) {
        ListaResponse creada = service.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // Listar todas las listas (200 OK)
    @GetMapping
    @Operation(summary = "Listar listas", description = "Obtiene todas las listas existentes")
    public List<ListaResponse> listar() {
        return service.listar();
    }

    // Obtener una lista por ID (200 OK)
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una lista", description = "Muestra una lista puntual por su identificador")
    public ListaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    // Ver los favoritos de una lista (200 OK)
    @GetMapping("/{id}/favoritos")
    @Operation(summary = "Favoritos de una lista", description = "Obtiene todos los favoritos que pertenecen a una lista")
    public List<FavoritoResponse> obtenerFavoritos(@PathVariable Long id) {
        return service.obtenerFavoritos(id);
    }

    // Eliminar una lista vacía (204 No Content, o 409 Conflict si tiene favoritos)
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar lista vacía", description = "Elimina una lista solo si no contiene favoritos asociados")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
