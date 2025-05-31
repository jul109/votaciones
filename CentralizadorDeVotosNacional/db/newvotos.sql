CREATE TABLE IF NOT EXISTS votos_recibidos (
    id TEXT PRIMARY KEY,
    id_candidato INT NOT NULL,
    id_mesa INT NOT NULL
);

CREATE OR REPLACE VIEW resumen_votos AS
SELECT
    id_candidato,
    COUNT(*) AS cantidad_votos
FROM votos_recibidos
GROUP BY id_candidato;
