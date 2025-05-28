package EleccionesLocal;

import com.zeroc.Ice.*;
import Votacion.*;
import java.sql.*;

public class Server {
    public static void main(String[] args) {
        int status = 0;
        try (Communicator communicator = Util.initialize(args)) {

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("EleccionesAdapter", "default -p 10009");

            // Conexión a la base de datos local
            java.sql.Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/eleccionesdb", "postgres", "postgres");

            // Crear e iniciar el servicio Elecciones
            EleccionesI eleccionesServant = new EleccionesI(conn);
            adapter.add(eleccionesServant, Util.stringToIdentity("Elecciones"));
            adapter.activate();

            System.out.println("🗳️ Servidor Elecciones Local activo en el puerto 10009...");

            // Conexión con el centralizador de votos nacional
            ObjectPrx base = communicator.stringToProxy("Replicador:default -h 192.168.131.110 -p 10010");
            ReplicadorPrx replicador = ReplicadorPrx.checkedCast(base);

            // Iniciar hilo de envío periódico de resultados
            new ResultadosReplicadorTask(conn, replicador).start();

            communicator.waitForShutdown();

        } catch (java.lang.Exception e) {
            e.printStackTrace();
            status = 1;
        }

        System.exit(status);
    }
}

