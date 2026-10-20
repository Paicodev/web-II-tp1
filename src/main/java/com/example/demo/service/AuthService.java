package com.example.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.RegistroRequest;
import com.example.demo.dto.UsuarioResponse;
import com.example.demo.exception.EmailYaExisteException;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.exception.CredencialesInvalidasException;
import com.example.demo.service.JwtService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    //Inyectamos el encoder aparte del repo
    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UsuarioResponse registrar(RegistroRequest request) {
        // Valido que no exista el email
        if (usuarioRepository.existePorEmail(request.email())) {
            throw new EmailYaExisteException("El email ya se encuentra registrado: " + request.email());
        }

        // Hasheo la contraseña con BCrypt
        String passwordHasheada = passwordEncoder.encode(request.password());

        // Crear el nuevo usuario con rol USER por defecto
        Usuario nuevo = new Usuario(
                null,
                request.email().toLowerCase().trim(),
                passwordHasheada,
                Rol.USER
        );

        // Guarda el usuario en PostgreSQL
        Usuario guardado = usuarioRepository.guardar(nuevo);

        return new UsuarioResponse(guardado.id(), guardado.email(), guardado.rol());
    }

    public LoginResponse login(LoginRequest request) {
        // Busca el usuario por email (si no existe -> 401)
        Usuario usuario = usuarioRepository.buscarPorEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new CredencialesInvalidasException("Email o contraseña incorrectos"));
        // Valida la contraseña cruda contra el hash de la BD usando BCrypt
        if (!passwordEncoder.matches(request.password(), usuario.passwordHash())) {
            throw new CredencialesInvalidasException("Email o contraseña incorrectos");
        }
        // Credenciales válidas -> emitir token JWT firmado
        String token = jwtService.generarToken(usuario);
        return new LoginResponse(token);
    }
}
