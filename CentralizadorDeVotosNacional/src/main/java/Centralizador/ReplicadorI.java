package Centralizador;

import Votacion.*;
import com.zeroc.Ice.Current;
import java.sql.*;
import java.util.*;

public class ReplicadorI implements Replicador {

    private final List<ObserverPrx> observers = new ArrayList<>();
    private final Connection conn;

    public ReplicadorI() throws Exception {
        conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/votosnacionales", "postgres", "postgres"
        );
    }

    @Override
    public void recibirResultados(Resultado[] resultados, Current current) {
        System.out.println("⚠️ Este canal ya no recibe votos directamente (deshabilitado por Reliable Messaging)");
    }

    @Override
    public void suscribirse(ObserverPrx observer, Current current) {
        observers.add(observer);
        System.out.println("🧷 Observer suscrito");
    }

    @Override
    public void desuscribirse(ObserverPrx observer, Current current) {
        observers.remove(observer);
        System.out.println("❌ Observer desuscrito");
    }

    private void notificarSubs() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_candidato, COUNT(*) as cantidad FROM votos_recibidos GROUP BY id_candidato")) {

            List<Resultado> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Resultado(rs.getInt("id_candidato"), rs.getInt("cantidad")));
            }

            for (ObserverPrx obs : observers) {
                obs.actualizar(lista.toArray(new Resultado[0]));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
