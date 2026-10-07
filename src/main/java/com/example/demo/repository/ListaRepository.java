package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import com.example.demo.model.Lista;

public interface ListaRepository{
    List<Lista> buscarTodos();
    Optional<Lista> buscarPorId(Long id);
    Lista guardar(Lista lista);
    void eliminar(Long id);
}