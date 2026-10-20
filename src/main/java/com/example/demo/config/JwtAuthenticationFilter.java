package com.example.demo.config;

import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Obtener el header Authorization
        String authHeader = request.getHeader("Authorization");

        // Si no hay encabezado o no empieza con "Bearer ", continúa la cadena
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token (sacando los primeros 7 caracteres: "Bearer ")
        String token = authHeader.substring(7);

        // Si el token es válido criptográficamente y no expiró
        if (jwtService.esTokenValido(token)) {
            String email = jwtService.extraerEmail(token);
            String rol = jwtService.extraerRol(token);

            // En Spring Security los roles usan por convención el prefijo ROLE_ (ej: ROLE_USER)
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));

            // Creamos el objeto de autenticación
            var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Guardamos la identidad del usuario en el contexto de seguridad del hilo actual
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continúa hacia el siguiente filtro o Controller
        filterChain.doFilter(request, response);
    }
}
