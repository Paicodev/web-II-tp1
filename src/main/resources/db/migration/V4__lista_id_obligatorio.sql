-- 1. Crear una lista por defecto si no existe
INSERT INTO listas (nombre)
VALUES ('Sin clasificar');

-- 2. Asignar esa lista a todos los favoritos huérfanos que tengan lista_id nulo
UPDATE favoritos
SET lista_id = (SELECT id FROM listas WHERE nombre = 'Sin clasificar' LIMIT 1)
WHERE lista_id IS NULL;

-- 3. Ahora que no hay ningún valor nulo, declarar la columna como obligatoria (NOT NULL)
ALTER TABLE favoritos
ALTER COLUMN lista_id SET NOT NULL;
