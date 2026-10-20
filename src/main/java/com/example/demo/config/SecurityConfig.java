package com.example.demo.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.
UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

     public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }
    // Este Bean nos permite inyectar PasswordEncoder en cualquier servicio
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    //Temporalmente liberaremos las rutas /api/auth/ y /api/productos/ y Swagger UI para poder probar el Login
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                //CSRF se deshabilita porque si no no podemos hacer peticiones con postman
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Si alguien no se autentica devolvemos 401 en vez de 403
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))).authorizeHttpRequests(auth -> auth
                        // Rutas públicas: auth, catálogo de productos y Swagger UI
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/productos/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // Todo lo demás exige estar autenticado
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
