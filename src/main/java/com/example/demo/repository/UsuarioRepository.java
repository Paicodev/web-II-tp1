package com.example.demo.repository;

import java.util.Optional;

import com.example.demo.model.Usuario;

public interface UsuarioRepository {
    Usuario guardar(Usuario usuario);
    Optional<Usuario> buscarPorId(Long id);
    Optional<Usuario> buscarPorEmail(String email);
    boolean existePorEmail(String email);
}
