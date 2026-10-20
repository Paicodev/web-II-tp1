package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    // Leemos las propiedades que configuramos en application.properties
    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration:3600000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Generar un JWT firmado con id y rol del usuario
    public String generarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", usuario.id());
        claims.put("rol", usuario.rol().name());

        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)                     // Datos adicionales: id y rol
                .subject(usuario.email())           // El "sujeto" principal del token (su email)
                .issuedAt(ahora)                    // Fecha de emisión
                .expiration(expiracion)             // Fecha de expiración
                .signWith(key)                      // Firma criptográfica con HMAC-SHA256
                .compact();                         // Compacta a formato String: header.payload.signature
    }

    // Extraer el email (subject) del token
    public String extraerEmail(String token) {
        return extraerClaims(token).getSubject();
    }

    // Extraer el rol del token
    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }

    // Extraer el id del usuario del token
    public Long extraerId(String token) {
        return extraerClaims(token).get("id", Long.class);
    }

    // Validar que la firma sea legítima y que no esté vencido
    public boolean esTokenValido(String token) {
        try {
            Claims claims = extraerClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false; // Firma inválida, token alterado o expirado
        }
    }

    // Método privado que parsea y verifica la firma con nuestra clave
    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
