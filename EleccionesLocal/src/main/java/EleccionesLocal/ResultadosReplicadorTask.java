package EleccionesLocal;

import Votacion.*;
import com.zeroc.Ice.*;
import java.sql.*;
import java.util.*;

public class ResultadosReplicadorTask extends Thread {
    private final java.sql.Connection conn;
    private final ReplicadorPrx replicador;

    public ResultadosReplicadorTask(java.sql.Connection conn, ReplicadorPrx replicador) {
        this.conn = conn;
        this.replicador = replicador;
    }

    @Override
    public void run() {
        while (true) {
            try {
                List<Resultado> resultados = new ArrayList<>();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT id_candidato, COUNT(*) as votos FROM votos GROUP BY id_candidato");

                while (rs.next()) {
                    Resultado r = new Resultado(rs.getInt("id_candidato"), rs.getInt("votos"));
                    resultados.add(r);
                }

                replicador.recibirResultados(resultados.toArray(new Resultado[0]));
                Thread.sleep(5000);
            } catch (java.lang.Exception e) {
                e.printStackTrace();
            }
        }
    }
}

