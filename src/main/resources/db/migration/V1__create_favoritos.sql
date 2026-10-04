CREATE TABLE favoritos(
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    nota VARCHAR(255),
    fecha_alta TIMESTAMP NOT NULL
);