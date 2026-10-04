package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.FavoritoEntity;

// JpaRepository<TipoDeEntidad, TipoDelId>
public interface FavoritoJpaRepository extends JpaRepository<FavoritoEntity, Long> {
    // Spring Data ya nos facilita findAll(), findById(), save(), deleteById(), etc.
}
