package EleccionesLocal;

import com.zeroc.Ice.*;
import Votacion.*;
import votacionRM.*;
import java.sql.*;

public class Server {


    public static void main(String[] args) {
        int status = 0;
        try (Communicator communicator = Util.initialize(args)) {
            java.sql.Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/eleccionesdb_3", "postgres", "postgres");


            ObjectAdapter mesaAdapter = communicator.createObjectAdapter("EleccionesAdapter");

            // Obtener propiedades
            Properties properties = communicator.getProperties();
            
            // Crear e insertar CentralizadorRM
            CentralizadorRMImpl centralizadorRMServant = new CentralizadorRMImpl(conn);
            Identity idCentralizador = Util.stringToIdentity("ReliableVotacion");
            mesaAdapter.add(centralizadorRMServant, idCentralizador);
            
            // Crear e insertar Elecciones
            EleccionesI eleccionesServant = new EleccionesI(conn);
            Identity idElecciones = Util.stringToIdentity("votaciones");
            mesaAdapter.add(eleccionesServant, idElecciones);
            
            // Activar adaptador
            mesaAdapter.activate();





            System.out.println("🗳️ Elecciones Local iniciado...");
            System.out.println("1.5");

            // Reliable Messaging setup
            ObjectAdapter rmAdapter = communicator.createObjectAdapterWithEndpoints("RMAdapter", "default -p 11003");

            // ACK local
            ACKVotoServiceI ackServant = new ACKVotoServiceI(conn);
            rmAdapter.add(ackServant, Util.stringToIdentity("ACKService"));
            rmAdapter.activate();


            

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
