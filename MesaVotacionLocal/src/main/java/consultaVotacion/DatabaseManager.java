package consultaVotacion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/votaciones";
    private static final String USER = "postgres";
    private static final String PASS = "1212";

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
        String query = "SELECT mesa_id FROM ciudadano WHERE documento = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, document);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("mesa_id");
            } else {
                return "No se encontró el documento en la base de datos";
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying database: " + e.getMessage());
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
