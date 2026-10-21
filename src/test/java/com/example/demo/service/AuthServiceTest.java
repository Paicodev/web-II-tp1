package com.example.demo.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegistroRequest;
import com.example.demo.dto.UsuarioResponse;
import com.example.demo.exception.CredencialesInvalidasException;
import com.example.demo.exception.EmailYaExisteException;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, passwordEncoder, jwtService);
    }

    @Test
    void registrar_debeHashearPasswordYGuardarUsuario() {
        // GIVEN (Dado)
        RegistroRequest request = new RegistroRequest("test@demo.com", "password123");
        when(usuarioRepository.existePorEmail("test@demo.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword123");
        when(usuarioRepository.guardar(any(Usuario.class)))
                .thenAnswer(inv -> new Usuario(1L, "test@demo.com", "$2a$10$hashedPassword123", Rol.USER));

        // WHEN (Cuando)
        UsuarioResponse response = authService.registrar(request);

        // THEN (Entonces)
        assertNotNull(response);
        assertEquals("test@demo.com", response.email());
        assertEquals(Rol.USER, response.rol());

        // Verificamos que se llamó a encode y NUNCA se guardó la contraseña en texto plano
        verify(passwordEncoder).encode("password123");
        verify(usuarioRepository).guardar(any(Usuario.class));
    }

    @Test
    void registrar_conEmailExistente_debeLanzarExcepcion() {
        // GIVEN
        RegistroRequest request = new RegistroRequest("repetido@demo.com", "password123");
        when(usuarioRepository.existePorEmail("repetido@demo.com")).thenReturn(true);

        // WHEN & THEN
        assertThrows(EmailYaExisteException.class, () -> authService.registrar(request));

        // Verificamos que ni siquiera intentó hashear ni guardar nada
        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    void login_conPasswordIncorrecta_debeLanzarExcepcion() {
        // GIVEN
        LoginRequest request = new LoginRequest("user@demo.com", "claveErronea");
        Usuario usuarioGuardado = new Usuario(1L, "user@demo.com", "$2a$10$hashReal", Rol.USER);

        when(usuarioRepository.buscarPorEmail("user@demo.com")).thenReturn(Optional.of(usuarioGuardado));
        when(passwordEncoder.matches("claveErronea", "$2a$10$hashReal")).thenReturn(false);

        // WHEN & THEN
        assertThrows(CredencialesInvalidasException.class, () -> authService.login(request));
        verify(jwtService, never()).generarToken(any());
    }

    @Test
    void login_conCredencialesValidas_debeRetornarToken() {
        // GIVEN
        LoginRequest request = new LoginRequest("user@demo.com", "claveCorrecta");
        Usuario usuarioGuardado = new Usuario(1L, "user@demo.com", "$2a$10$hashReal", Rol.USER);

        when(usuarioRepository.buscarPorEmail("user@demo.com")).thenReturn(Optional.of(usuarioGuardado));
        when(passwordEncoder.matches("claveCorrecta", "$2a$10$hashReal")).thenReturn(true);
        when(jwtService.generarToken(usuarioGuardado)).thenReturn("mocked.jwt.token");

        // WHEN
        LoginResponse response = authService.login(request);

        // THEN
        assertNotNull(response);
        assertEquals("token_roto_para_probar_ci", response.token());
        verify(jwtService).generarToken(usuarioGuardado);
    }
}
