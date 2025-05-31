package Centralizador;

import com.zeroc.Ice.*;
import votacionRM.*;
import Votacion.*;

public class Server {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {

            // Adapter tradicional con Observer
            ObjectAdapter adapter1 = communicator.createObjectAdapterWithEndpoints("ReplicadorAdapter", "default -p 10010");
            adapter1.add(new ReplicadorI(), Util.stringToIdentity("Replicador"));
            adapter1.activate();

            // Adapter para Reliable Message
            ObjectAdapter adapter2 = communicator.createObjectAdapterWithEndpoints("CentralizadorRM", "default -p 10012");
            adapter2.add(new CentralizadorRMImpl(), Util.stringToIdentity("CentralizadorRM"));
            adapter2.activate();

            System.out.println("🌐 Centralizador Nacional activo en puertos 10010 (Observer) y 10012 (Reliable)");
            communicator.waitForShutdown();

        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }
}
