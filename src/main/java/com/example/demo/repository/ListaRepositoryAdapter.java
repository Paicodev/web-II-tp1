package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.demo.entity.ListaEntity;
import com.example.demo.entity.UsuarioEntity;
import com.example.demo.model.Lista;

@Repository
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository jpaRepository;

    public ListaRepositoryAdapter(ListaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Lista> buscarTodos() {
        return jpaRepository.findAll()
                .stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Optional<Lista> buscarPorId(Long id) {
        return jpaRepository.findById(id)
                .map(this::aDominio);
    }

    @Override
    public Lista guardar(Lista lista) {
        ListaEntity entity = aEntity(lista);
        ListaEntity guardada = jpaRepository.save(entity);
        return aDominio(guardada);
    }

    @Override
    public void eliminar(Long id) {
        jpaRepository.deleteById(id);
    }

    // --- Mapeadores ---
    private Lista aDominio(ListaEntity entity) {
        Long usuarioId = entity.getUsuario() != null ? entity.getUsuario().getId(): null;
        return new Lista(entity.getId(), entity.getNombre(), usuarioId);
    }

    private ListaEntity aEntity(Lista domain) {
        ListaEntity entity = new ListaEntity(domain.id(), domain.nombre());
        if (domain.usuarioId() != null) {
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setId(domain.usuarioId());
            entity.setUsuario(usuario);
        }
        return entity;
    }

    @Override
    public List<Lista> buscarPorUsuarioId(Long usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::aDominio)
                .toList();
    }
}
