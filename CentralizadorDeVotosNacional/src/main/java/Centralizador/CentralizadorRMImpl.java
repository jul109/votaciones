package Centralizador;

import votacionRM.*;
import com.zeroc.Ice.Current;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


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

    public void terminarJornada() {
        try {
            // Crear carpetas si no existen
            Files.createDirectories(Paths.get("resultados/mesas"));

            generarResumenGeneral();
            generarResumenPorMesa();

            System.out.println("📁 Archivos CSV generados exitosamente en /resultados");

        } catch (Exception e) {
            System.err.println("❌ Error generando archivos CSV:");
            e.printStackTrace();
        }
    }

    private void generarResumenGeneral() throws SQLException, IOException {
        String query = "SELECT id_candidato, c.nombre AS nombre, COUNT(*) AS total " +
                    "FROM votos_recibidos v JOIN candidatos c ON v.id_candidato = c.id " +
                    "GROUP BY id_candidato, c.nombre";

        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            PrintWriter writer = new PrintWriter("resultados/resume.csv")) {

            writer.println("candidateId,candidateName,totalVotes");
            while (rs.next()) {
                writer.printf("%d,%s,%d%n", rs.getInt("id_candidato"), rs.getString("nombre"), rs.getInt("total"));
            }
        }
    }

    private void generarResumenPorMesa() throws SQLException, IOException {
        String mesasQuery = "SELECT DISTINCT id_mesa FROM votos_recibidos";
        try (Statement stmtMesas = conn.createStatement();
            ResultSet rsMesas = stmtMesas.executeQuery(mesasQuery)) {

            while (rsMesas.next()) {
                int mesaId = rsMesas.getInt("id_mesa");
                String query = "SELECT id_candidato, c.nombre AS nombre, COUNT(*) AS total " +
                            "FROM votos_recibidos v JOIN candidatos c ON v.id_candidato = c.id " +
                            "WHERE id_mesa = ? GROUP BY id_candidato, c.nombre";

                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setInt(1, mesaId);
                    try (ResultSet rs = ps.executeQuery();
                        PrintWriter writer = new PrintWriter("resultados/mesas/partial-" + mesaId + ".csv")) {

                        writer.println("candidateId,candidateName,totalVotes");
                        while (rs.next()) {
                            writer.printf("%d,%s,%d%n", rs.getInt("id_candidato"), rs.getString("nombre"), rs.getInt("total"));
                        }
                    }
                }
            }
        }
    }


}


