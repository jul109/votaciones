package EleccionesLocal;

import votacionRM.*;
import com.zeroc.Ice.Current;
import java.sql.*;

public class CentralizadorRMImpl implements CentralizadorRM {
    private final Connection conn;

    public CentralizadorRMImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void recibirVoto(Voto voto, ACKVotoServicePrx ack, Current current) {
        System.out.println("RECIBI VOTACION VIEJO ");
        try {
            // Generar un id localmente, igual que en EleccionesI
            String id = voto.id != null ? voto.id : java.util.UUID.randomUUID().toString();

            // Insertar en la tabla votos (registro histórico)
            PreparedStatement ps1 = conn.prepareStatement(
                "INSERT INTO votos (id_candidato, id_mesa, fecha) VALUES (?, ?, current_timestamp)");
            ps1.setInt(1, voto.idCandidato);
            ps1.setInt(2, voto.idMesa);
            ps1.executeUpdate();

            // Insertar en la tabla votos_pendientes (para replicación)
            PreparedStatement ps2 = conn.prepareStatement(
                "INSERT INTO votos_pendientes (id, id_candidato, id_mesa, enviado) VALUES (?, ?, ?, false)");
            ps2.setString(1, id);
            ps2.setInt(2, voto.idCandidato);
            ps2.setInt(3, voto.idMesa);
            ps2.executeUpdate();

            ack.ack(id);
            System.out.println("✅ Voto confiable recibido y registrado en EleccionesLocal: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}