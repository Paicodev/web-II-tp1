-- Insertamos un usuario administrador por defecto si no existe ninguno con ese email
-- La password hasheada con BCrypt corresponde a: admin123
INSERT INTO usuarios (email, password_hash, rol)
VALUES ('admin@demo.com', '$2a$10$i4eU2tYGOZuqiS1vw5dW5eFPxwKrYnUopPKtZvrCqNYGmA2s.8HX2', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

-- Agregamos la columna usuario_id permitiendo nulos temporalmente
ALTER TABLE listas
ADD COLUMN usuario_id BIGINT;

-- Backfill: asignamos las listas existentes sin dueño al usuario administrador
UPDATE listas
SET usuario_id = (SELECT id FROM usuarios WHERE email = 'admin@demo.com' LIMIT 1)
WHERE usuario_id IS NULL;

-- Ahora que todas las filas tienen dueño, hacemos la columna obligatoria
ALTER TABLE listas
ALTER COLUMN usuario_id SET NOT NULL;

-- Agregamos la clave foránea vinculando con la tabla usuarios
ALTER TABLE listas
ADD CONSTRAINT fk_listas_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios(id);
