package com.example.demo.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.servlet.Filter;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApiIntegrationTest {

    // Testcontainers levanta una imagen oficial de PostgreSQL 17 en Docker
    // @ServiceConnection autoconfigura la conexión de Spring Boot dinámicamente
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Inicializamos MockMvc acoplado con la cadena de filtros de seguridad
        if (this.mockMvc == null) {
            this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                    .addFilter(springSecurityFilterChain)
                    .build();
        }

        // Registramos un usuario común si aún no existe
        String registroJson = """
            {
                "email": "usuario.test@demo.com",
                "password": "password123"
            }
            """;
        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registroJson));

        // Obtenemos el token del usuario común
        String loginUserJson = """
            {
                "email": "usuario.test@demo.com",
                "password": "password123"
            }
            """;
        String responseUser = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginUserJson))
                .andReturn().getResponse().getContentAsString();
        this.userToken = objectMapper.readTree(responseUser).get("token").asText();

        // Obtenemos el token del admin (sembrado por la migración V6)
        String loginAdminJson = """
            {
                "email": "admin@demo.com",
                "password": "admin123"
            }
            """;
        String responseAdmin = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginAdminJson))
                .andReturn().getResponse().getContentAsString();
        this.adminToken = objectMapper.readTree(responseAdmin).get("token").asText();
    }

    @Test
    @DisplayName("Peticin a ruta protegida sin token debe responder 401 Unauthorized")
    void getListas_sinToken_debeRetornar401() throws Exception {
        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login con credenciales invlidas debe responder 401 Unauthorized")
    void login_credencialesInvalidas_debeRetornar401() throws Exception {
        String jsonInvalido = """
            {
                "email": "usuario.test@demo.com",
                "password": "clave_totalmente_incorrecta"
            }
            """;
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Usuario comn puede crear una lista y se le asigna su usuarioId")
    void crearLista_conTokenUser_debeCrearListaConDuenio() throws Exception {
        String crearListaJson = """
            {
                "nombre": "Mis Libros Favoritos"
            }
            """;
        mockMvc.perform(post("/api/listas")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(crearListaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Mis Libros Favoritos"))
                .andExpect(jsonPath("$.usuarioId").isNumber());
    }

    @Test
    @DisplayName("Usuario comn intentando acceder a lista ajena recibe 403 Forbidden")
    void getListaAjena_conTokenUser_debeRetornar403() throws Exception {
        // La lista con ID 1 pertenece al administrador (creada en la migracin V4/V6)
        mockMvc.perform(get("/api/listas/1")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acceso denegado"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Administrador puede acceder a la lista 1 de forma legtima")
    void getLista_conTokenAdmin_debeRetornar200() throws Exception {
        mockMvc.perform(get("/api/listas/1")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
