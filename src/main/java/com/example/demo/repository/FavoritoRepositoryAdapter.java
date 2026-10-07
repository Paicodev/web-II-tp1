package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.FavoritoEntity;
import com.example.demo.entity.ListaEntity;
import com.example.demo.model.Favorito;

@Repository
public class FavoritoRepositoryAdapter implements FavoritoRepository {

    private final FavoritoJpaRepository jpaRepository;

    public FavoritoRepositoryAdapter(FavoritoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Favorito> buscarTodos() {
        return jpaRepository.findAll()
                .stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Optional<Favorito> buscarPorId(Long id) {
        return jpaRepository.findById(id)
                .map(this::aDominio);
    }

    @Override
    public Favorito guardar(Favorito favorito) {
        FavoritoEntity entity = aEntity(favorito);
        FavoritoEntity guardada = jpaRepository.save(entity);
        return aDominio(guardada);
    }

    @Override
    public void eliminar(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Favorito> buscarPorListaId(Long listaId) {
        return jpaRepository.findByListaId(listaId)
                .stream()
                .map(this::aDominio)
                .toList();
    }

    // --- Mapeadores privados (van una sola vez al final) ---

    // De Entidad JPA a Modelo de Dominio
    private Favorito aDominio(FavoritoEntity entity) {
        return new Favorito(
                entity.getId(),
                entity.getProductoId(),
                entity.getLista() != null ? entity.getLista().getId() : null,
                entity.getNota(),
                entity.getFechaAlta().toLocalDate()
        );
    }

    // De Modelo de Dominio a Entidad JPA
    private FavoritoEntity aEntity(Favorito domain) {
        FavoritoEntity entity = new FavoritoEntity(
                domain.id(),
                domain.productoId(),
                domain.nota(),
                domain.fecha() != null ? domain.fecha().atStartOfDay() : java.time.LocalDateTime.now()
        );
        if (domain.listaId() != null) {
            entity.setLista(new ListaEntity(domain.listaId(), null));
        }
        return entity;
    }
}
