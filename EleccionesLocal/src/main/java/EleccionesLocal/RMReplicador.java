package EleccionesLocal;

import votacionRM.*;
import java.sql.*;

public class RMReplicador extends Thread {
    private final Connection conn;
    private final CentralizadorRMPrx centralizador;
    private final ACKVotoServicePrx ackService;

    public RMReplicador(Connection conn, CentralizadorRMPrx centralizador, ACKVotoServicePrx ackService) {
        this.conn = conn;
        this.centralizador = centralizador;
        this.ackService = ackService;
    }

    @Override
    public void run() {
        while (true) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, id_candidato, id_mesa FROM votos_pendientes WHERE enviado = false");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Voto voto = new Voto(rs.getString("id"), rs.getInt("id_candidato"), rs.getInt("id_mesa"));
                    try {
                        centralizador.recibirVoto(voto, ackService);
                        System.out.println("📤 Enviado voto ID: " + voto.id);
                    } catch (Exception e) {
                        System.err.println("❌ Error enviando voto ID " + voto.id + ": " + e.getMessage());
                    }
                }

                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
