module Votacion {

    struct Candidato {
        int id;
        string nombre;
        string partido;
    };

    sequence<Candidato> CandidatosList;

    interface Elecciones {
        CandidatosList obtenerCandidatos();
        void registrarVoto(int idCandidato, int idMesa);
    };

};


