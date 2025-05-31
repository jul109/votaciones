package EleccionesLocal;

import votacionRM.*;
import com.zeroc.Ice.Current;
import java.sql.*;

public class ACKVotoServiceI implements ACKVotoService {
    private final Connection conn;

    public ACKVotoServiceI(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void ack(String votoId, Current current) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE votos_pendientes SET enviado = true WHERE id = ?")) {
            ps.setString(1, votoId);
            ps.executeUpdate();
            System.out.println("✅ ACK recibido para voto ID: " + votoId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
