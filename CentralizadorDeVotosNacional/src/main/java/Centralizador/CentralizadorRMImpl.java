package Centralizador;

import votacionRM.*;
import com.zeroc.Ice.Current;
import java.sql.*;

public class CentralizadorRMImpl implements CentralizadorRM {
    private final Connection conn;

    public CentralizadorRMImpl() throws SQLException {
        this.conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/votosnacionales", "postgres", "postgres"
        );
    }

    @Override
    public void recibirVoto(Voto voto, ACKVotoServicePrx ack, Current current) {
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO votos_recibidos (id, id_candidato, id_mesa) VALUES (?, ?, ?) ON CONFLICT (id) DO NOTHING")) {

            ps.setString(1, voto.id);
            ps.setInt(2, voto.idCandidato);
            ps.setInt(3, voto.idMesa);
            ps.executeUpdate();

            ack.ack(voto.id);
            System.out.println("✅ Voto confiable recibido: " + voto.id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
