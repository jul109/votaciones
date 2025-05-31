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
        System.out.println("Registrando voto: Candidato ID " + idCandidato + ", Mesa ID " + idMesa);
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO votos (id_candidato, id_mesa) VALUES (?, ?)")) {
            ps.setInt(1, idCandidato);
            ps.setInt(2, idMesa);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

