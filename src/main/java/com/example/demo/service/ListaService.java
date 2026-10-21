package com.example.demo.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.CrearListaRequest;
import com.example.demo.dto.FavoritoResponse;
import com.example.demo.dto.ListaResponse;
import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.exception.ListaNoVaciaException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Favorito;
import com.example.demo.model.Lista;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.repository.UsuarioRepository;

@Service
public class ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;

    public ListaService(
            ListaRepository listaRepository,
            FavoritoRepository favoritoRepository,
            UsuarioRepository usuarioRepository) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Crear lista: se asigna automǭticamente el usuario autenticado como dueo
    public ListaResponse crear(CrearListaRequest request) {
        Usuario actual = obtenerUsuarioAutenticado();
        Lista nueva = new Lista(null, request.nombre(), actual.id());
        return aResponse(listaRepository.guardar(nueva));
    }

    // Listar: ADMIN ve todas las listas; USER solo ve las suyas
    public List<ListaResponse> listar() {
        Usuario actual = obtenerUsuarioAutenticado();
        List<Lista> listas;
        if (actual.rol() == Rol.ADMIN) {
            listas = listaRepository.buscarTodos();
        } else {
            listas = listaRepository.buscarPorUsuarioId(actual.id());
        }
        return listas.stream()
                .map(this::aResponse)
                .toList();
    }

    // Buscar una lista por ID: verifica existencia (404) y permisos (403)
    public ListaResponse buscar(Long id) {
        Usuario actual = obtenerUsuarioAutenticado();
        Lista lista = listaRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la lista " + id));
        validarPropiedad(lista, actual);
        return aResponse(lista);
    }

    // Ver favoritos de una lista: valida que exista y sea de su propiedad
    public List<FavoritoResponse> obtenerFavoritos(Long listaId) {
        buscar(listaId); // Valida existencia (404) y propiedad (403)
        return favoritoRepository.buscarPorListaId(listaId)
                .stream()
                .map(this::aFavoritoResponse)
                .toList();
    }

    // Eliminar una lista vaca (valida existencia, propiedad y 409 si tiene favoritos)
    public void eliminar(Long id) {
        buscar(id); // Valida existencia (404) y propiedad (403)
        if (!favoritoRepository.buscarPorListaId(id).isEmpty()) {
            throw new ListaNoVaciaException("No se puede eliminar la lista porque todava contiene favoritos");
        }
        listaRepository.eliminar(id);
    }

    // Operacin Atmica para mover favoritos
    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        // Valida que ambas listas existan y pertenezcan al usuario (o que sea ADMIN)
        buscar(origenId);
        buscar(destinoId);

        List<Favorito> favoritos = favoritoRepository.buscarPorListaId(origenId);

        for (Favorito fav : favoritos) {
            Favorito reasignado = new Favorito(
                    fav.id(),
                    fav.productoId(),
                    destinoId,
                    fav.nota(),
                    fav.fecha()
            );
            favoritoRepository.guardar(reasignado);
        }

        listaRepository.eliminar(origenId);
    }

    // --- MǸtodos auxiliares de seguridad ---

    // Obtiene el usuario autenticado a partir del SecurityContextHolder
    private Usuario obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new AccesoDenegadoException("No hay usuario autenticado en la sesin");
        }
        return usuarioRepository.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado en la base de datos"));
    }

    // Valida que la lista pertenezca al usuario o que sea ADMIN
    private void validarPropiedad(Lista lista, Usuario actual) {
        if (actual.rol() != Rol.ADMIN && !lista.usuarioId().equals(actual.id())) {
            throw new AccesoDenegadoException("No tienes permiso para acceder a esta lista");
        }
    }

    // --- Traductores a DTO ---
    private ListaResponse aResponse(Lista lista) {
        return new ListaResponse(lista.id(), lista.nombre(), lista.usuarioId());
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
}
