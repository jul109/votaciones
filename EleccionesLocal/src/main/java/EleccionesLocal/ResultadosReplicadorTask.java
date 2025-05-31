package EleccionesLocal;

import Votacion.*;
import com.zeroc.Ice.*;
import java.sql.*;
import java.util.*;

public class ResultadosReplicadorTask extends Thread {
    private final java.sql.Connection conn;
    private final ReplicadorPrx replicador;
    private Timestamp ultimoEnvio;

    public ResultadosReplicadorTask(java.sql.Connection conn, ReplicadorPrx replicador) {
        this.conn = conn;
        this.replicador = replicador;
        this.ultimoEnvio = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public void run() {
        while (true) {
            try {
                List<Resultado> resultados = new ArrayList<>();

                // Agrupar solo votos nuevos desde la última vez
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_candidato, COUNT(*) as votos FROM votos " +
                    "WHERE fecha > ? GROUP BY id_candidato"
                );
                ps.setTimestamp(1, ultimoEnvio);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    resultados.add(new Resultado(
                        rs.getInt("id_candidato"),
                        rs.getInt("votos")
                    ));
                }

                // Solo enviar si hay algo nuevo
                if (!resultados.isEmpty()) {
                    replicador.recibirResultados(resultados.toArray(new Resultado[0]));
                    ultimoEnvio = new Timestamp(System.currentTimeMillis());
                }

                Thread.sleep(5000);
            } catch (java.lang.Exception e) {
                e.printStackTrace();
            }
        }
    }
}
