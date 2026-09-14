package com.example.demo.service;

import com.example.demo.client.dummyjson.DummyJsonClient;
import com.example.demo.client.dummyjson.DummyJsonProducto;
import com.example.demo.dto.ProductoDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

//Anotador para registrarlo como Bean de servicios
@Service 
public class ProductoService {

    //lo hacemos final para que no lo cambiemos y lo inyectamos por constructor
    private final DummyJsonClient client;

    public ProductoService(DummyJsonClient client) {
        this.client = client;
    }

    
    private ProductoDTO mapearADto(DummyJsonProducto externo) {
        //se crea un producto con los atributos del producto externo
        return new ProductoDTO(
                externo.id(),
                externo.title(),                            // title -> nombre
                externo.description(),                      // description -> descripcion
                BigDecimal.valueOf(externo.price()),        // double -> BigDecimal
                externo.category()                          // category -> categoria
        );
    }

    //En cada operacion mapeamos a DTO para solo abstraer los datos que nos interesan    
    public List<ProductoDTO> listarTodos() {
        return client.obtenerTodos()
                .stream()
                .map(this::mapearADto) 
                .toList();
    }

    public ProductoDTO buscarPorId(Long id) {
        DummyJsonProducto productoExterno = client.obtenerPorId(id);
        return mapearADto(productoExterno);
    }

}

