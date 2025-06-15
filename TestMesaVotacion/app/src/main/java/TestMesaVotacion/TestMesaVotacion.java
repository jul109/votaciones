package TestMesaVotacion;

import VotacionTest.VoteStationPrx;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestMesaVotacion {

    public static void main(String[] args) {
        Communicator communicator = null;
        CsvManager csvManager = new CsvManager(); // asegúrate de que CsvManager esté bien implementado
        Map<String, VoteStationPrx> voteStationProxies = new HashMap<>();

        try {
            communicator = Util.initialize(args);
            System.out.println("✅ ICE communicator inicializado");

            Map<String, VotoTest> votosCargados = csvManager.getVotesMap();
            if (votosCargados == null || votosCargados.isEmpty()) {
                System.out.println("⚠️ No se encontraron votos en 'votos.csv'.");
                return;
            }

            // Mostrar votos
            for (VotoTest voto : votosCargados.values()) {
                System.out.println("📥 Voto cargado: " + voto);
            }

            // Crear conjunto de IP:PUERTO únicos
            Set<String> uniqueIpPorts = new HashSet<>();
            for (VotoTest voto : votosCargados.values()) {
                uniqueIpPorts.add(voto.getIpMesa() + ":" + voto.getPuerto());
            }

            // Crear proxies
            System.out.println("\n🔌 Inicializando proxies de VoteStation...");
            for (String ipPort : uniqueIpPorts) {
                String[] parts = ipPort.split(":");
                if (parts.length != 2) {
                    System.err.println("❌ Formato inválido para IP:PUERTO → " + ipPort);
                    continue;
                }

                String ip = parts[0];
                String port = parts[1];
                String proxyStr = "VoteStation_Mesa:tcp -h " + ip + " -p " + port;

                try {
                    ObjectPrx base = communicator.stringToProxy(proxyStr);
                    VoteStationPrx proxy = VoteStationPrx.checkedCast(base);

                    if (proxy != null) {
                        voteStationProxies.put(ipPort, proxy);
                        System.out.println("  ✅ Proxy creado para " + ipPort);
                    } else {
                        System.err.println("  ⚠️ No se pudo castear el proxy para " + ipPort);
                    }
                } catch (Exception e) {
                    System.err.println("  ❌ Error al crear proxy para " + ipPort + ": " + e.getMessage());
                }
            }

            // Resumen
            if (voteStationProxies.isEmpty()) {
                System.out.println("🚫 No se pudo inicializar ningún proxy de VoteStation.");
            } else {
                System.out.println("\n🧾 Proxies inicializados:");
                voteStationProxies.forEach((ipPort, proxy) -> System.out.println("  - " + ipPort));
            }

            consumeVoteStationService(votosCargados, voteStationProxies);

        } catch (Exception e) {
            System.err.println("💥 Error en cliente de TestMesaVotacion:");
            e.printStackTrace();
        } finally {
            if (communicator != null) {
                try {
                    communicator.destroy();
                    System.out.println("🛑 ICE communicator cerrado.");
                } catch (Exception e) {
                    System.err.println("Error al cerrar ICE: " + e.getMessage());
                }
            }
        }
    }

    public static void consumeVoteStationService(
            Map<String, VotoTest> votesFromCsv,
            Map<String, VoteStationPrx> voteStationProxies) {
        // Aquí puedes iterar y enviar los votos a cada proxy correspondiente
    }
}
