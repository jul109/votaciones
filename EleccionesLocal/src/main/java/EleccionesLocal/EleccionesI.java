package EleccionesLocal;

import Votacion.*;
import com.zeroc.Ice.Current;
import java.sql.*;
import java.util.*;

public class EleccionesI implements Elecciones {
    private final Connection conn;

    public EleccionesI(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Candidato[] obtenerCandidatos(Current current) {
        List<Candidato> candidatos = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM candidatos")) {
            while (rs.next()) {
                candidatos.add(new Candidato(rs.getInt("id"), rs.getString("nombre"), rs.getString("partido")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return candidatos.toArray(new Candidato[0]);
    }

    @Override
    public void registrarVoto(int idCandidato, int idMesa, Current current) {
        try {
            String id = UUID.randomUUID().toString();

            PreparedStatement ps1 = conn.prepareStatement(
                "INSERT INTO votos (id_candidato, id_mesa, fecha) VALUES (?, ?, current_timestamp)");
            ps1.setInt(1, idCandidato);
            ps1.setInt(2, idMesa);
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement(
                "INSERT INTO votos_pendientes (id, id_candidato, id_mesa, enviado) VALUES (?, ?, ?, false)");
            ps2.setString(1, id);
            ps2.setInt(2, idCandidato);
            ps2.setInt(3, idMesa);
            ps2.executeUpdate();

            System.out.println("🗳️ Voto registrado en votos_pendientes: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
