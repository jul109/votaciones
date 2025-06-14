package Centralizador;

import com.zeroc.Ice.*;
import votacionRM.*;
import Votacion.*;

import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {

            // Adapters
            ObjectAdapter adapter1 = communicator.createObjectAdapterWithEndpoints("ReplicadorAdapter", "default -p 10010");
            adapter1.add(new ReplicadorI(), Util.stringToIdentity("Replicador"));
            adapter1.activate();

            CentralizadorRMImpl centralizadorRM = new CentralizadorRMImpl();
            ObjectAdapter adapter2 = communicator.createObjectAdapterWithEndpoints("CentralizadorRM", "default -p 10012");
            adapter2.add(centralizadorRM, Util.stringToIdentity("CentralizadorRM"));
            adapter2.activate();

            System.out.println("🌐 Centralizador Nacional activo.");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\nOprime 1 para terminar la jornada de votación y exportar resultados:");
                String input = scanner.nextLine();

                if ("1".equals(input)) {
                    System.out.println("🛑 Terminando jornada...");
                    centralizadorRM.terminarJornada();
                    break;
                }
            }

            communicator.waitForShutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

