\c eleccionesdb

CREATE TABLE candidatos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    partido VARCHAR(100)
);

INSERT INTO candidatos (nombre, partido) VALUES
('Carlos Pérez', 'Partido Azul'),
('Ana Gómez', 'Partido Verde'),
('Luis Rodríguez', 'Partido Rojo');
