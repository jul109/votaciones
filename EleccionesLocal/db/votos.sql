
CREATE TABLE IF NOT EXISTS votos (
    id SERIAL PRIMARY KEY,
    id_candidato INT NOT NULL,
    id_mesa INT NOT NULL,
    fecha TIMESTAMP DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS votos_pendientes (
    id TEXT PRIMARY KEY,
    id_candidato INT NOT NULL,
    id_mesa INT NOT NULL
);