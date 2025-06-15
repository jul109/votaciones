package MesaVotacion;

import VotacionTest.*;
import com.zeroc.Ice.Current;

public class VoteStationImp implements VoteStation { 

    public VoteStationImp() {
        System.out.println("VoteStationImp inicializado. Listo para recibir llamadas.");
    }

    @Override
    public int vote(String document, int candidateId, Current current) {
        System.out.println("VoteStationImp: Recibiendo voto remoto para documento: " + document + ", candidato ID: " + candidateId);
        System.out.println("VoteStationImp: Siempre retornando 3 (respuesta de prueba).");
        return 3;
    }
}