module votacionRM {

    struct Voto {
        string id;
        int idCandidato;
        int idMesa;
    };

    sequence<Voto> ListaVotos;

    interface ACKVotoService {
        void ack(string votoId);
    };

    interface CentralizadorRM {
        void recibirVoto(Voto voto, ACKVotoService* ack);
    };
};
