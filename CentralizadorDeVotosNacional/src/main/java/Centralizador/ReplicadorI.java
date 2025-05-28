package Centralizador;

import Votacion.*;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.ObjectPrx;

import java.sql.*;
import java.util.*;

public class ReplicadorI implements Replicador {

    private final List<ObserverPrx> observers = new ArrayList<>();
    private final Connection conn;

    public ReplicadorI() throws Exception {
        conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/votosnacionales",
            "postgres", "postgres"
        );
    }

    public void recibirResultados(Resultado[] resultados, Current current) {
        try {
            for (Resultado r : resultados) {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO votos (id_candidato, cantidad) VALUES (?, ?) " +
                    "ON CONFLICT (id_candidato) DO UPDATE SET cantidad = votos.cantidad + EXCLUDED.cantidad"
                );
                ps.setInt(1, r.idCandidato);
                ps.setInt(2, r.cantidadVotos);
                ps.executeUpdate();
                System.out.printf("✅ Candidato %d: +%d votos%n", r.idCandidato, r.cantidadVotos);
            }

            notificarSubs();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void notificarSubs() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM votos")) {

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

    public void suscribirse(Votacion.ObserverPrx observer, Current current) {
    	observers.add(observer);
        System.out.println("🧷 Observer suscrito");
    }

    public void desuscribirse(Votacion.ObserverPrx observer, Current current) {
    	observers.remove(observer);
	System.out.println("Observer desuscrito");
	}

}
