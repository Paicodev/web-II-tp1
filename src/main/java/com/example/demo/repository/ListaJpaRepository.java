package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ListaEntity;

public interface ListaJpaRepository extends JpaRepository<ListaEntity, Long> {
}
