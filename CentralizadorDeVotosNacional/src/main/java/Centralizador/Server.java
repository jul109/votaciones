package Centralizador;

import com.zeroc.Ice.*;

public class Server {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("ReplicadorAdapter", "default -p 10010");
            adapter.add(new ReplicadorI(), Util.stringToIdentity("Replicador"));
            adapter.activate();
            System.out.println("🌐 Centralizador Nacional listo en puerto 10010");
            communicator.waitForShutdown();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }
}
