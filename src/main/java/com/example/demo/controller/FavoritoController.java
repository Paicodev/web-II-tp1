package com.example.demo.controller;

import com.example.demo.dto.CrearFavoritoRequest;
import com.example.demo.dto.FavoritoResponse;
import com.example.demo.service.FavoritoService;


import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

//Es el mas complejo de los tres, es el que se comunica con el cliente, recibe las solicitudes y devuelve las respuestas.
/*Usa muchos anotadores:
-@RestController: indica que es un controlador REST, es decir, que devuelve datos en formato JSON.
-@RequestMapping("/api/Favoritos"): indica la ruta base de las solicitudes, es decir, que todas las solicitudes que empiecen con /api/Favoritos se van a manejar en este controlador.
-@GetMapping: indica que el metodo maneja solicitudes GET, es decir, que devuelve datos.
-@PostMapping: indica que el metodo maneja solicitudes POST, es decir, que recibe datos.
-@PutMapping: indica que el metodo maneja solicitudes PUT, es decir, que actualiza datos.
-@DeleteMapping: indica que el metodo maneja solicitudes DELETE, es decir, que elimina datos.
-ResponseEntity: es una clase que representa una respuesta HTTP, es decir, que contiene el codigo de estado, los encabezados y el cuerpo de la respuesta.
*/
@RestController
@RequestMapping("/api/favoritos")
@Tag(name="Favoritos", description = "Operaciones CRUD sobre la lista de favoritos en memoria")
public class FavoritoController {
    private final FavoritoService service;

    public FavoritoController(FavoritoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary="Listar favoritos", description = "Lista todos los favoritos que se guardaron")
    public List<FavoritoResponse> listar(
            @RequestParam(defaultValue = "") String q) {
        return service.listar(q);
    }

    @GetMapping("/{id}")
    @Operation(summary="Buscar por Id", description = "Muestra el favorito específico segun el id ingresado")
    public FavoritoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary="Agregar a favoritos", description = "Agrega un producto de la api externa a favoritos y se le asocia una nota personal")
    public ResponseEntity<FavoritoResponse> crear(
            @Valid @RequestBody CrearFavoritoRequest request) {
        FavoritoResponse creado = service.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary="Modifica un favorito", description = "Permite actualizar los datos de un producto en el listado de favoritos")
    public FavoritoResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CrearFavoritoRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary="Eliminar favorito", description = "permite eliminar un favorito del listado en memoria")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}