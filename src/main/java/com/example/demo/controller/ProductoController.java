package com.example.demo.controller;

import com.example.demo.dto.ProductoDTO;
import com.example.demo.service.ProductoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController 
@RequestMapping("/api/productos") 
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    
    @GetMapping
    public List<ProductoDTO> listar() {
        return service.listarTodos();
    }

    
    @GetMapping("/{id}")
    public ProductoDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }
}
