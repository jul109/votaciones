package MesaVotacion;

import VotacionTest.*;
import com.zeroc.Ice.Current;

public class VoteStationImp implements VoteStation { 
    private Controlador controlador;

    public VoteStationImp(Controlador controlador) {
        this.controlador=controlador;
        System.out.println("VoteStationImp inicializado. Listo para recibir llamadas.");
    }

    @Override
    public int vote(String document, int candidateId, Current current) {
        return controlador.votarTest(document, candidateId);
    }
}