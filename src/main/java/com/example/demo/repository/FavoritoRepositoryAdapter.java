package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.demo.entity.FavoritoEntity;
import com.example.demo.model.Favorito;

@Repository
public class FavoritoRepositoryAdapter implements FavoritoRepository {

    private final FavoritoJpaRepository jpaRepository;

    // Inyección por constructor (DI)
    public FavoritoRepositoryAdapter(FavoritoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

//Sobreescribimos los metodos del puerto para usar la base de datos con el JPA Repository
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

    //aca lo que hacemos es tomar la entidad JPA y convertirla a modelo de dominio para poder devolverla
    private Favorito aDominio(FavoritoEntity entity) {
        return new Favorito(
                entity.getId(),
                entity.getProductoId(),
                entity.getNota(),
                entity.getFechaAlta().toLocalDate()
        );
    }

    //aca lo que hacemos es tomar el modelo de dominio y convertirlo a entidad JPA para poder guardarlo en la base de datos
    private FavoritoEntity aEntity(Favorito domain) {
        return new FavoritoEntity(
                domain.id(),
                domain.productoId(),
                domain.nota(),
                domain.fecha() != null ? domain.fecha().atStartOfDay() : java.time.LocalDateTime.now()
        );
 }
}