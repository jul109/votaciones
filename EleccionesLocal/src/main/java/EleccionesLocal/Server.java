package EleccionesLocal;

import com.zeroc.Ice.*;
import Votacion.*;
import votacionRM.*;
import java.sql.*;

public class Server {

        // PENDING

        private static void inicializarRecepcionConfiableMesaServer(Communicator communicator, java.sql.Connection conn) throws java.lang.Exception {
        System.out.println("🔗 Inicializando recepción confiable desde Mesa de Votación...");
        
        // Adapter para recibir votos confiables - NOMBRE DIFERENTE
        ObjectAdapter mesaAdapter = communicator.createObjectAdapterWithEndpoints("MesaAdapter", "tcp -h 192.168.131.112 -p 10017");

        // Servant para recibir votos confiables desde la Mesa de Votación
        CentralizadorRMImpl centralizadorRMServant = new CentralizadorRMImpl(conn);
        mesaAdapter.add(centralizadorRMServant, Util.stringToIdentity("CentralizadorRM_Mesa"));
        
        mesaAdapter.activate();
        
        System.out.println("✅ Recepción confiable lista - Puerto 10017");
    }


    public static void main(String[] args) {
        int status = 0;
        try (Communicator communicator = Util.initialize(args)) {

            // Adapter para Elecciones
            ObjectAdapter adapter = communicator.createObjectAdapter("EleccionesAdapter");
            java.sql.Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/eleccionesdb", "postgres", "postgres");

            EleccionesI eleccionesServant = new EleccionesI(conn);
            Properties properties = communicator.getProperties();
            Identity id = Util.stringToIdentity(properties.getProperty("Identity"));
            adapter.add(eleccionesServant, id);
            adapter.activate();

            System.out.println("🗳️ Elecciones Local iniciado...");
            System.out.println("1.5");

            // Reliable Messaging setup
            ObjectAdapter rmAdapter = communicator.createObjectAdapterWithEndpoints("RMAdapter", "default -p 10011");

            // ACK local
            ACKVotoServiceI ackServant = new ACKVotoServiceI(conn);
            rmAdapter.add(ackServant, Util.stringToIdentity("ACKService"));
            rmAdapter.activate();

            inicializarRecepcionConfiableMesaServer(communicator, conn);
            

            // Proxy remoto
            ObjectPrx remote = communicator.stringToProxy("CentralizadorRM:default -h 192.168.131.110 -p 10012");
            CentralizadorRMPrx centralizadorRM = CentralizadorRMPrx.checkedCast(remote);

            // Proxy local a ACK
            ObjectPrx ackBase = rmAdapter.createProxy(Util.stringToIdentity("ACKService"));
            ACKVotoServicePrx ackProxy = ACKVotoServicePrx.checkedCast(ackBase);

            new RMReplicador(conn, centralizadorRM, ackProxy).start();

            communicator.waitForShutdown();

        } catch (java.lang.Exception e) {
            e.printStackTrace();
            status = 1;
        }

        System.exit(status);
    }
}
