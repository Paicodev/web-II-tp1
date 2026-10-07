package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.CrearListaRequest;
import com.example.demo.dto.FavoritoResponse;
import com.example.demo.dto.ListaResponse;
import com.example.demo.exception.ListaNoVaciaException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Favorito;
import com.example.demo.model.Lista;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;

@Service
public class ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;

    public ListaService(ListaRepository listaRepository, FavoritoRepository favoritoRepository) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
    }

    // Crear lista
    public ListaResponse crear(CrearListaRequest request) {
        Lista nueva = new Lista(null, request.nombre());
        return aResponse(listaRepository.guardar(nueva));
    }

    // Listar todas, usa streams para transformar las listas a DTOs y map a una lista de DTOs
    public List<ListaResponse> listar() {
        return listaRepository.buscarTodos()
                .stream()
                .map(this::aResponse)
                .toList();
    }

    // Buscar una lista por ID, lanza 404 si no existe
    public ListaResponse buscar(Long id) {
        return listaRepository.buscarPorId(id)
                .map(this::aResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la lista " + id));
    }

    // Ver los favoritos de una lista puntual
    public List<FavoritoResponse> obtenerFavoritos(Long listaId) {
        buscar(listaId); // Valida que la lista exista (404 si no)
        return favoritoRepository.buscarPorListaId(listaId)
                .stream()
                .map(this::aFavoritoResponse)
                .toList();
    }

    // Eliminar una lista vacía (409 si tiene favoritos)
    public void eliminar(Long id) {
        buscar(id); // Valida que exista
        if (!favoritoRepository.buscarPorListaId(id).isEmpty()) {
            throw new ListaNoVaciaException("No se puede eliminar la lista porque todavía contiene favoritos");
        }
        listaRepository.eliminar(id);
    }

    // --- Traductores a DTO ---
    private ListaResponse aResponse(Lista lista) {
        return new ListaResponse(lista.id(), lista.nombre());
    }

    private FavoritoResponse aFavoritoResponse(Favorito favorito) {
        return new FavoritoResponse(
                favorito.id(),
                favorito.productoId(),
                favorito.listaId(),
                favorito.nota(),
                favorito.fecha()
        );
    }

    // Operacion Atomica que mueve todos los fav o no mueve nada
    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        //Valida que ambas listas existan (tira 404 si alguna no está gracias a que buscar() lanza exception)
        buscar(origenId);
        buscar(destinoId);

        //Traer los favoritos de la lista origen
        List<Favorito> favoritos = favoritoRepository.buscarPorListaId(origenId);

        //Reasignar cada favorito a la lista destino y guardarlo
        for (Favorito fav : favoritos) {
            Favorito reasignado = new Favorito(
                    fav.id(),
                    fav.productoId(),
                    destinoId, // lista nueva que sale de MoverFavoritosDTO
                    fav.nota(),
                    fav.fecha()
            );
            favoritoRepository.guardar(reasignado);
        }

        // Eliminar la lista origen
        listaRepository.eliminar(origenId);
    }

}
