package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.demo.entity.FavoritoEntity;

// JpaRepository<TipoDeEntidad, TipoDelId>
public interface FavoritoJpaRepository extends JpaRepository<FavoritoEntity, Long> {
    
    //Aparte de los metodos que vienen con JpaRepository, podemos crear nuestros propios metodos con el principio de Query Methods
    //findByNombreDeLaPropiedad
    List<FavoritoEntity> findByListaId(Long listaId);
    }
