package TestMesaVotacion;

import VotacionTest.VoteStationPrx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class VoteDispatcher {

    private static final int DELAY_ENTRE_VOTOS_MS = 2000; // 2 segundos

    public static void consumirServicioVotacion(
            Map<String, List<VotoTest>> votosPorIpPort,
            Map<String, VoteStationPrx> proxiesPorIpPort) {

        int nProxies = proxiesPorIpPort.size();
        ExecutorService executor = Executors.newFixedThreadPool(nProxies);
        List<ResultadoVoto> resultados = new CopyOnWriteArrayList<>();

        for (Map.Entry<String, VoteStationPrx> proxyEntry : proxiesPorIpPort.entrySet()) {
            String ipPort = proxyEntry.getKey();
            VoteStationPrx proxy = proxyEntry.getValue();
            List<VotoTest> votos = votosPorIpPort.get(ipPort);

            if (votos == null || votos.isEmpty()) {
                System.out.println("ℹ️ No hay votos para " + ipPort);
                continue;
            }

            executor.submit(() -> {
                System.out.println("🧵 Enviando votos a " + ipPort);
                for (VotoTest voto : votos) {
                    long inicio = System.nanoTime();
                    int respuesta = -1;

                    try {
                        respuesta = proxy.vote(voto.getDocumentoVotante(), voto.getIdCandidato());
                        System.out.println("✅ Enviado: " + voto + " → respuesta: " + respuesta);
                    } catch (Exception e) {
                        System.err.println("❌ Error enviando " + voto + " → " + e.getMessage());
                    }

                    long duracionMs = (System.nanoTime() - inicio) / 1_000_000;
                    resultados.add(new ResultadoVoto(
                            voto.getDocumentoVotante(),
                            voto.getIdCandidato(),
                            voto.getIpMesa(),
                            voto.getPuerto(),
                            duracionMs,
                            respuesta
                    ));

                    try {
                        Thread.sleep(DELAY_ENTRE_VOTOS_MS);
                    } catch (InterruptedException e) {
                        System.err.println("⚠️ Interrumpido " + ipPort);
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("🏁 Finalizados los votos de " + ipPort);
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                System.err.println("⏱️ Tiempo agotado. Pool forzado a cerrar.");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            System.err.println("⚠️ Pool interrumpido.");
        }

        new CsvManager().guardarResultados(resultados);
    }
}
