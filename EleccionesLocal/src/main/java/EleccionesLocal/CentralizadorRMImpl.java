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
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO votos_pendientes (id, id_candidato, id_mesa, enviado) VALUES (?, ?, ?, false) ON CONFLICT (id) DO NOTHING")) {

            ps.setString(1, voto.id);
            ps.setInt(2, voto.idCandidato);
            ps.setInt(3, voto.idMesa);
            ps.executeUpdate();

            ack.ack(voto.id);
            System.out.println("✅ Voto confiable recibido en EleccionesLocal: " + voto.id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}