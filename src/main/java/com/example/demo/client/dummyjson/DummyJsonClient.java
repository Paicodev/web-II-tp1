package com.example.demo.client.dummyjson;

import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.exception.ServicioExternoException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import com.example.demo.client.dummyjson.DummyJsonProducto;
import com.example.demo.client.dummyjson.DummyJsonProductoResponse;

import java.util.List;

@Component
public class DummyJsonClient {
    private final RestClient restClient;

    public DummyJsonClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<DummyJsonProducto> obtenerTodos(){
        try{
            DummyJsonProductoResponse response = restClient.get().uri("/products").retrieve().body(DummyJsonProductoResponse.class);
            return response != null ? response.products() : List.of();
        }catch (ResourceAccessException e){
            throw new ServicioExternoException("No se pudo conectar con el catálogo de productos", e);
    }
}
public DummyJsonProducto obtenerPorId(Long id) {
        try {
            return restClient.get()
                    .uri("/products/{id}", id)
                    .retrieve()
                    .body(DummyJsonProducto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            // Si DummyJSON devuelve 404 porque el id no existe
            throw new RecursoNoEncontradoException("No existe el producto con id: " + id);
        } catch (ResourceAccessException ex) {
            // Si se corta internet o da timeout
            throw new ServicioExternoException("No se pudo conectar con el catálogo de productos", ex);
        }
    }
}