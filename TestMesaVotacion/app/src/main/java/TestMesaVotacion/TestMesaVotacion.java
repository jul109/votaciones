package TestMesaVotacion;

import VotacionTest.VoteStationPrx;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import java.util.*;

public class TestMesaVotacion {

    public static void main(String[] args) {
        Communicator communicator = null;
        Map<String, VoteStationPrx> proxiesPorIpPort = new HashMap<>();
        CsvManager csvManager = new CsvManager();

        try {
            communicator = Util.initialize(args);
            System.out.println("✅ ICE communicator inicializado");

            // Obtener votos agrupados por IP:Puerto
            Map<String, List<VotoTest>> votosPorIpPort = csvManager.getVotesByIpAndPortMap();
            if (votosPorIpPort.isEmpty()) {
                System.out.println("⚠️ No se encontraron votos en el archivo CSV.");
                return;
            }

            // Mostrar votos cargados
            votosPorIpPort.forEach((ipPort, votos) -> {
                System.out.println("📥 Votos para " + ipPort + ":");
                votos.forEach(System.out::println);
            });

            // Crear proxies por cada IP:Puerto única
            for (String ipPort : votosPorIpPort.keySet()) {
                String[] parts = ipPort.split(":");
                if (parts.length != 2) {
                    System.err.println("❌ Formato inválido de IP:PUERTO → " + ipPort);
                    continue;
                }

                String ip = parts[0];
                String puerto = parts[1];
                String proxyStr = "VoteStation_Mesa:tcp -h " + ip + " -p " + puerto;

                try {
                    ObjectPrx base = communicator.stringToProxy(proxyStr);
                    VoteStationPrx proxy = VoteStationPrx.checkedCast(base);
                    if (proxy != null) {
                        proxiesPorIpPort.put(ipPort, proxy);
                        System.out.println("🔌 Proxy conectado: " + ipPort);
                    } else {
                        System.err.println("⚠️ Proxy nulo para " + ipPort);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error al crear proxy para " + ipPort + ": " + e.getMessage());
                }
            }

            // Verificar si se crearon proxies
            if (proxiesPorIpPort.isEmpty()) {
                System.out.println("🚫 No se pudo conectar a ningún proxy.");
                return;
            }

            
            VoteDispatcher.consumirServicioVotacion(votosPorIpPort, proxiesPorIpPort);


        } catch (Exception e) {
            System.err.println("💥 Error en TestMesaVotacion:");
            e.printStackTrace();
        } finally {
            if (communicator != null) {
                communicator.destroy();
                System.out.println("🛑 ICE communicator cerrado.");
            }
        }
    }

    
}
