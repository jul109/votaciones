package TestMesaVotacion;

import VotacionTest.VoteStationPrx;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;
import java.util.Map;

public class TestMesaVotacion {

    public static void main(String[] args) {
        CsvManager csvManager = new CsvManager();
        Map<String, VotoTest> votosCargados = csvManager.getVotesMap();

        if (votosCargados.isEmpty()) {
            System.out.println("No se encontraron votos o hubo un error al cargar 'votos.csv'.");
        } else {
            for (VotoTest voto : votosCargados.values()) {
                System.out.println("Voto cargado: " + voto);
            }
        }

        consumeVoteStationService(votosCargados);
    }

    public static void consumeVoteStationService(Map<String, VotoTest> votesFromCsv) {
        Communicator communicator = null;
        VoteStationPrx voteStationProxy = null;
        
        try {
            communicator = Util.initialize(new String[]{}); 
            System.out.println("Comunicador Ice inicializado para el cliente.");

            // The proxy string now includes the hostname x206m03
            String voteStationProxyString = "VoteStation_Mesa:default -h x206m03 -p 10015"; 
            voteStationProxy = VotacionTest.VoteStationPrx.checkedCast(
                                   communicator.stringToProxy(voteStationProxyString));

            if (voteStationProxy == null) {
                System.err.println("ERROR: No se pudo obtener el proxy para VoteStation.");
                System.err.println("Asegúrate de que el servidor esté ejecutándose en 'x206m03' en el puerto '10015'");
                System.err.println("con la identidad 'VoteStation_Mesa'.");
                throw new Exception("Proxy de VoteStation no disponible.");
            }
            System.out.println("Proxy de VoteStation obtenido. ¡Listo para enviar votos al servidor remoto!");

            int totalVotosEnviados = 0;
            for (VotoTest voto : votesFromCsv.values()) {
                String document = voto.getDocumentoVotante(); 
                int candidateId = voto.getIdCandidato(); 

                System.out.print("Enviando voto (Doc: " + document + ", Cand: " + candidateId + ") -> ");
                try {
                    int result = voteStationProxy.vote(document, candidateId);
                    System.out.println("Resultado del servicio: " + result);
                    totalVotosEnviados++;
                } catch (com.zeroc.Ice.LocalException e) {
                    System.err.println("Error de comunicación ICE al enviar voto para " + document + " (servidor remoto): " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error al enviar voto para " + document + " (servidor remoto): " + e.getMessage());
                }
            }
            System.out.println("Total de votos enviados al servicio remoto: " + totalVotosEnviados);

        } catch (Exception e) {
            System.err.println("Error grave durante la ejecución del cliente de servicio:");
            e.printStackTrace();
        } finally {
            if (communicator != null) {
                try {
                    communicator.destroy();
                    System.out.println("Comunicador Ice del cliente cerrado.");
                } catch (Exception e) {
                    System.err.println("Error al cerrar el comunicador Ice del cliente: " + e.getMessage());
                }
            }
        }
    }
}