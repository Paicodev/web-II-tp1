package com.example.demo.controller;

import com.example.demo.dto.ProductoDTO;
import com.example.demo.service.ProductoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController 
@RequestMapping("/api/productos") 

@Tag(name ="Productos", description = "Catálogo de productos (obtenidos de la API DummyJSON)")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    
    @GetMapping
    @Operation(summary = "Mostrar catálogo", description="Obtiene todos los productos de la API externa mapeados a nuestro propio formato")
    public List<ProductoDTO> listar() {
        return service.listarTodos();
    }

    
    @GetMapping("/{id}")
    @Operation(summary = "Busca producto por id", description="Obtiene el producto específico según la id que se ingrese")
    public ProductoDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }
}
