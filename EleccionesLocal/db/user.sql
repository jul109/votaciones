-- Crear usuario con contraseña
CREATE USER elecciones_user WITH PASSWORD 'voto2025';

-- Crear base de datos
CREATE DATABASE eleccionesdb OWNER elecciones_user;

-- Otorgar permisos
GRANT ALL PRIVILEGES ON DATABASE eleccionesdb TO elecciones_user;