package MesaVotacion;



import votacionRM.*;
import com.zeroc.Ice.LocalException;

public class MedidorVoto {

    public static long medirTiempoEnvioVoto(Voto voto, CentralizadorRMPrx centralizador, ACKVotoServicePrx ackService) throws Exception {
        if (centralizador == null) {
            throw new IllegalArgumentException("❌ El proxy del centralizador no puede ser null");
        }

        long inicio = System.nanoTime();
        centralizador.recibirVoto(voto, ackService);
        long fin = System.nanoTime();

        long duracionMs = (fin - inicio) / 1_000_000;

        System.out.println("✅ Voto enviado (ID: " + voto.id + ") en " + duracionMs + " ms. ACK recibido: " );

        return duracionMs;
    }
}
