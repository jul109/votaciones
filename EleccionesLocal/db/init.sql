\c eleccionesdb

CREATE TABLE candidatos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    partido VARCHAR(100)
);

CREATE TABLE votos (
    id SERIAL PRIMARY KEY,
    id_candidato INT REFERENCES candidatos(id),
    id_mesa INT
);

INSERT INTO candidatos (nombre, partido) VALUES
('Carlos Pérez', 'Partido Azul'),
('Ana Gómez', 'Partido Verde'),
('Luis Rodríguez', 'Partido Rojo');
