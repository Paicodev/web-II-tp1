package com.example.demo.service;

import com.example.demo.dto.CrearFavoritoRequest;
import com.example.demo.dto.FavoritoResponse;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.model.Favorito;
import com.example.demo.exception.RecursoNoEncontradoException;

import java.util.List;
import org.springframework.stereotype.Service;
@Service
public class FavoritoService {
    private final FavoritoRepository repository;

    public FavoritoService(FavoritoRepository repository) {
        this.repository = repository;
    }

    public List<FavoritoResponse> listar(String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase();

        return repository.buscarTodos().stream()
            .filter(favorito -> favorito.nota().toLowerCase().contains(filtro))
            .map(this::aResponse)
            .toList();
    }

    public FavoritoResponse buscar(Long id) {
        return repository.buscarPorId(id)
            .map(this::aResponse)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No existe el Favorito " + id
            ));
    }

    public FavoritoResponse crear(CrearFavoritoRequest request) {
        Favorito nuevo = new Favorito(
            null, request.productoId(), request.nota(), java.time.LocalDate.now()
        );
        return aResponse(repository.guardar(nuevo));
    }

    public FavoritoResponse actualizar(Long id, CrearFavoritoRequest request) {
        buscar(id);
        Favorito actualizado = new Favorito(
            id, request.productoId(), request.nota(), java.time.LocalDate.now()
        );
        return aResponse(repository.guardar(actualizado));
    }

    public void eliminar(Long id) {
        buscar(id);
        repository.eliminar(id);
    }

    private FavoritoResponse aResponse(Favorito Favorito) {
        return new FavoritoResponse(
            Favorito.id(),
            Favorito.productoId(),
            Favorito.nota(),
            Favorito.fecha()
        );
    }
}
