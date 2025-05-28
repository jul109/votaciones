module Votacion {

    struct Candidato {
        int id;
        string nombre;
        string partido;
    };

    struct Resultado {
        int idCandidato;
        int cantidadVotos;
    };

    sequence<Candidato> CandidatosList;
    sequence<Resultado> ResultadosList;

    interface Elecciones {
        CandidatosList obtenerCandidatos();
        void registrarVoto(int idCandidato, int idMesa);
    };

    interface Replicador {
        void recibirResultados(ResultadosList resultados);
    };
};
