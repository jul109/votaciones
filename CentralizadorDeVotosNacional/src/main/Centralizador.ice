module Votacion {

    struct Resultado {
        int idCandidato;
        int cantidadVotos;
    };

    sequence<Resultado> ResultadosList;

    interface Observer {
        void actualizar(ResultadosList resultados);
    };

    interface Replicador {
        void recibirResultados(ResultadosList resultados);
        void suscribirse(Observer* observer);  
        void desuscribirse(Observer* observer);
    };
};
