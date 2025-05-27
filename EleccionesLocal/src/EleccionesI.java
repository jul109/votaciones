package EleccionesLocal;

import Votacion.*;
import com.zeroc.Ice.Current;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EleccionesI implements Elecciones {
    private Connection conn;

    public EleccionesI() throws Exception {
        String url = "jdbc:postgresql://localhost:5432/eleccionesdb";
        String user = "postgres";
        String password = "postgres"; // Reemplaza con tu contraseña
        conn = DriverManager.getConnection(url, user, password);
    }

    @Override
    public Candidato[] obtenerCandidatos(Current current) {
        List<Candidato> candidatos = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM candidatos")) {
            while (rs.next()) {
                Candidato c = new Candidato(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("partido")
                );
                candidatos.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return candidatos.toArray(new Candidato[0]);
    }

    @Override
    public void registrarVoto(int idCandidato, int idMesa, Current current) {
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO votos (id_candidato, id_mesa) VALUES (?, ?)")) {
            ps.setInt(1, idCandidato);
            ps.setInt(2, idMesa);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
