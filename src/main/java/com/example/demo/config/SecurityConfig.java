package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

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
                // 3. Reglas de acceso
                .authorizeHttpRequests(auth -> auth
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
                .build();
    }
}
