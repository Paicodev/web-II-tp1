package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.dto.CrearListaRequest;
import com.example.demo.dto.ListaResponse;
import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.exception.ListaNoVaciaException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Favorito;
import com.example.demo.model.Lista;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ListaServiceTest {

    @Mock
    private ListaRepository listaRepository;

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private ListaService listaService;

    @BeforeEach
    void setUp() {
        listaService = new ListaService(listaRepository, favoritoRepository, usuarioRepository);
    }

    @AfterEach
    void tearDown() {
        // Limpiamos el hilo de ejecucin para evitar contaminacin entre tests
        SecurityContextHolder.clearContext();
    }

    // Helper para simular un usuario logueado en el SecurityContext
    private void autenticarComo(Long id, String email, Rol rol) {
        var auth = new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(usuarioRepository.buscarPorEmail(email))
                .thenReturn(Optional.of(new Usuario(id, email, "hash", rol)));
    }

    @Test
    void crear_asignaDuenioDesdeUsuarioAutenticado() {
        // GIVEN: Usuario con ID 5 logueado
        autenticarComo(5L, "usuario@demo.com", Rol.USER);

        CrearListaRequest request = new CrearListaRequest("Mis Compras");
        when(listaRepository.guardar(any(Lista.class)))
                .thenAnswer(inv -> {
                    Lista guardada = inv.getArgument(0);
                    return new Lista(10L, guardada.nombre(), guardada.usuarioId());
                });

        // WHEN
        ListaResponse response = listaService.crear(request);

        // THEN: La lista creada debe pertenecer al usuario 5
        assertNotNull(response);
        assertEquals(5L, response.usuarioId());
        assertEquals("Mis Compras", response.nombre());
        verify(listaRepository).guardar(any(Lista.class));
    }

    @Test
    void buscar_usuarioComunAccedeAListaPropia_debeRetornarla() {
        // GIVEN: Usuario con ID 5 accede a su lista con dueo 5
        autenticarComo(5L, "usuario@demo.com", Rol.USER);
        Lista listaPropia = new Lista(1L, "Favoritos Personales", 5L);
        when(listaRepository.buscarPorId(1L)).thenReturn(Optional.of(listaPropia));

        // WHEN
        ListaResponse response = listaService.buscar(1L);

        // THEN
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(5L, response.usuarioId());
    }

    @Test
    void buscar_usuarioComunAccedeAListaAjena_debeLanzarAccesoDenegadoException() {
        // GIVEN: Usuario con ID 5 (USER) intenta acceder a lista del usuario 99
        autenticarComo(5L, "usuario@demo.com", Rol.USER);
        Lista listaAjena = new Lista(1L, "Lista Secreta", 99L);
        when(listaRepository.buscarPorId(1L)).thenReturn(Optional.of(listaAjena));

        // WHEN & THEN: Debe arrojar 403 Forbidden
        assertThrows(AccesoDenegadoException.class, () -> listaService.buscar(1L));
    }

    @Test
    void buscar_adminAccedeAListaAjena_debeRetornarla() {
        // GIVEN: Usuario con ID 1 (ADMIN) accede a lista de otro usuario (99L)
        autenticarComo(1L, "admin@demo.com", Rol.ADMIN);
        Lista listaAjena = new Lista(1L, "Lista de Usuario", 99L);
        when(listaRepository.buscarPorId(1L)).thenReturn(Optional.of(listaAjena));

        // WHEN
        ListaResponse response = listaService.buscar(1L);

        // THEN: Admin tiene permiso global
        assertNotNull(response);
        assertEquals(99L, response.usuarioId());
    }

    @Test
    void listar_usuarioComun_soloConsultaSusListas() {
        // GIVEN
        autenticarComo(5L, "usuario@demo.com", Rol.USER);
        when(listaRepository.buscarPorUsuarioId(5L))
                .thenReturn(List.of(new Lista(1L, "L1", 5L)));

        // WHEN
        List<ListaResponse> resultado = listaService.listar();

        // THEN
        assertEquals(1, resultado.size());
        verify(listaRepository).buscarPorUsuarioId(5L);
        verify(listaRepository, never()).buscarTodos();
    }

    @Test
    void listar_admin_consultaTodasLasListas() {
        // GIVEN
        autenticarComo(1L, "admin@demo.com", Rol.ADMIN);
        when(listaRepository.buscarTodos())
                .thenReturn(List.of(new Lista(1L, "L1", 5L), new Lista(2L, "L2", 99L)));

        // WHEN
        List<ListaResponse> resultado = listaService.listar();

        // THEN
        assertEquals(2, resultado.size());
        verify(listaRepository).buscarTodos();
        verify(listaRepository, never()).buscarPorUsuarioId(any());
    }

    @Test
    void eliminar_listaConFavoritos_debeLanzarListaNoVaciaException() {
        // GIVEN: Dueño legtimo intenta eliminar una lista pero tiene favoritos adentro
        autenticarComo(5L, "usuario@demo.com", Rol.USER);
        Lista lista = new Lista(1L, "Compras", 5L);
        when(listaRepository.buscarPorId(1L)).thenReturn(Optional.of(lista));

        Favorito fav = new Favorito(10L, 100L, 1L, "Nota", LocalDate.now());
        when(favoritoRepository.buscarPorListaId(1L)).thenReturn(List.of(fav));

        // WHEN & THEN: Debe arrojar 409 Conflict
        assertThrows(ListaNoVaciaException.class, () -> listaService.eliminar(1L));
        verify(listaRepository, never()).eliminar(any());
    }

    @Test
    void buscar_listaInexistente_debeLanzarRecursoNoEncontradoException() {
        // GIVEN
        autenticarComo(5L, "usuario@demo.com", Rol.USER);
        when(listaRepository.buscarPorId(999L)).thenReturn(Optional.empty());

        // WHEN & THEN: 404 Not Found
        assertThrows(RecursoNoEncontradoException.class, () -> listaService.buscar(999L));
    }
}
