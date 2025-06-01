package MesaVotacion;

import com.zeroc.Ice.Current;

public class ACKVotoServiceI implements votacionRM.ACKVotoService {
    private final CsvManager csvManager;

    public ACKVotoServiceI(CsvManager csvManager) {
        this.csvManager = csvManager;
    }

    @Override
    public void ack(String votoId, Current current) {
        
        try {
            // Eliminar el voto de la lista de pendientes en CSV
            csvManager.eliminarVotoPendiente(votoId);
            System.out.println("✅ ACK recibido para voto ID: " + votoId + " - Eliminado de pendientes");
        } catch (Exception e) {
            System.err.println("❌ Error al procesar ACK para voto ID " + votoId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}