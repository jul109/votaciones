package MesaVotacion;

import java.util.List;
import com.zeroc.Ice.LocalException;
import com.zeroc.IceGrid.*;

public class VotosRMTask extends Thread {
    private final CsvManager csvManager;
    private volatile votacionRM.CentralizadorRMPrx centralizador;
    private final votacionRM.ACKVotoServicePrx ackService;
    private final com.zeroc.Ice.Communicator communicator;
    private final Controlador controlador;

    public VotosRMTask(CsvManager csvManager, votacionRM.CentralizadorRMPrx centralizador,
                       votacionRM.ACKVotoServicePrx ackService, com.zeroc.Ice.Communicator communicator, 
                       Controlador controlador) {
        this.csvManager = csvManager;
        this.centralizador = centralizador;
        this.ackService = ackService;
        this.communicator = communicator;
        this.controlador = controlador;
    }

    @Override
    public void run() {
        System.out.println("INICIANDO BUCLE");
        while (true) {
            try {
                if (centralizador == null) {
                    System.out.println("🔄 VotosRMTask: Proxy al Centralizador Local es nulo o inválido, intentando recrear...");
                    Thread.sleep(1000);
                    controlador.inicializarComunicacionConfiable();
                    break;
                }

                List<VotoPendiente> pendientes = csvManager.getVotosPendientes();

                if (pendientes.isEmpty()) {
                    // System.out.println("VotosRMTask: No hay votos pendientes para enviar.");
                } else {
                    System.out.println("VotosRMTask: Procesando " + pendientes.size() + " votos pendientes.");
                }

                for (VotoPendiente votoPendiente : pendientes) {
                    votacionRM.Voto voto = new votacionRM.Voto(
                        votoPendiente.getId(),
                        votoPendiente.getCandidatoId(),
                        votoPendiente.getMesaId()
                    );

                    try {
                        centralizador.recibirVoto(voto, ackService);
                        System.out.println("📤 VotosRMTask: Enviado voto ID: " + voto.id);
                    } catch (LocalException iceEx) {
                        System.err.println("❌ VotosRMTask: Error de comunicación (Ice LocalException) al enviar voto ID " + voto.id + ": " + iceEx.getMessage());
                        centralizador = null;
                        break;
                    } catch (java.lang.Exception e) {
                        System.err.println("❌ VotosRMTask: Error inesperado al enviar voto ID " + voto.id + ": " + e.getMessage());
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("VotosRMTask: Hilo de envío de votos interrumpido. Terminando...");
                Thread.currentThread().interrupt();
                break;
            } catch (java.lang.Exception e) {
                System.err.println("❌ VotosRMTask: Error crítico en el bucle principal: " + e.getMessage());
                e.printStackTrace();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void recreateCentralizadorProxy() {
        try {
            com.zeroc.Ice.ObjectPrx baseCentralizador = communicator.stringToProxy("DemoIceGrid/Query");

            QueryPrx query = QueryPrx.checkedCast(baseCentralizador);
            System.out.println("Proxy de Query casteado correctamente");

            votacionRM.CentralizadorRMPrx newCentralizador = votacionRM.CentralizadorRMPrx.checkedCast(query.findObjectByType("::votacionRM::CentralizadorRM"));
            System.out.println("Proxy de Elecciones casteado correctamente");

            if (newCentralizador != null) {
                this.centralizador = newCentralizador;
                System.out.println("✅ VotosRMTask: Proxy al Centralizador Local recreado exitosamente.");
            } else {
                System.err.println("❌ VotosRMTask: checkedCast devolvió null al recrear proxy. El servicio remoto podría no estar disponible o tener la identidad incorrecta.");
                this.centralizador = null;
            }
        } catch (LocalException iceEx) {
            System.err.println("❌ VotosRMTask: Error de comunicación Ice al intentar recrear proxy: " + iceEx.getMessage());
            this.centralizador = null;
        } catch (java.lang.Exception e) {
            System.err.println("❌ VotosRMTask: Error inesperado al recrear proxy: " + e.getMessage());
            e.printStackTrace();
            this.centralizador = null;
        }
    }
}