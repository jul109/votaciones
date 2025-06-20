package consultaVotacion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:postgresql://192.168.131.22:5432/votaciones";
    private static final String USER = "postgres";
    private static final String PASS = "postgres";

    private Connection connection;

    public DatabaseManager() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error connecting to database: " + e.getMessage());
        }
    }

    public String queryVoter(String document) {
        String query = "SELECT mesa_id, puesto_id, nombre_puesto, direccion, municipio, departamento " +
                      "FROM ciudadano_detalle WHERE documento = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, document);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return String.format("Usted debe votar en \"%s\" ubicado en \"%s\" en \"%s\", \"%s\" en la mesa \"%s\"",
                    rs.getString("nombre_puesto"),
                    rs.getString("direccion"),
                    rs.getString("municipio"),
                    rs.getString("departamento"),
                    rs.getString("mesa_id"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying database: " + e.getMessage());
        }
    }

    public String[] obtenerCiudadanos(int mesaId) {
        String query = "SELECT documento FROM ciudadano WHERE mesa_id = ? ORDER BY documento";
        List<String> ciudadanos = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, mesaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String documento = rs.getString("documento");
                    if (documento != null && !documento.trim().isEmpty()) {
                        ciudadanos.add(documento.trim());
                    }
                }
            }
            
            if (ciudadanos.isEmpty()) {
                System.out.println("No se encontraron ciudadanos para la mesa: " + mesaId);
                return new String[0];
            }
            
            System.out.println("Se encontraron " + ciudadanos.size() + " ciudadanos para la mesa " + mesaId);
            return ciudadanos.toArray(new String[0]);
            
        } catch (SQLException e) {
            System.err.println("Error consultando ciudadanos: " + e.getMessage());
            throw new RuntimeException("Error consultando ciudadanos: " + e.getMessage(), e);
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}
