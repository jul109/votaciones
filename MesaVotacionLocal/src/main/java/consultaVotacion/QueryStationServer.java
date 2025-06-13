package consultaVotacion;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import com.zeroc.Ice.Exception;
import com.zeroc.Ice.Current;
import consultaVotacion.queryStation;
import consultaVotacion.queryStationI;

public class QueryStationServer {

    private static final int PORT = 10000;
    private static final String ADAPTER_NAME = "QueryStationAdapter";

    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            // Crear el administrador de base de datos
            DatabaseManager dbManager = new DatabaseManager();

            // Crear el adaptador de objetos
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                    ADAPTER_NAME, "tcp -p " + PORT);

            // Crear e instanciar el servidor
            queryStation queryStationServant = new queryStationI(dbManager);
            adapter.add(queryStationServant, Util.stringToIdentity("QueryStation"));

            // Activar el adaptador
            adapter.activate();

            System.out.println("Servidor iniciado en el puerto " + PORT);
            System.out.println("Presione Ctrl+C para terminar...");

            // Mantener el servidor en ejecución
            communicator.waitForShutdown();

            // Limpiar recursos
            dbManager.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class queryStationI implements queryStation {

    private DatabaseManager dbManager;

    public queryStationI(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public String query(String document, Current current) {
        System.out.println("Consulta recibida para documento: " + document);
        try {
            String result = dbManager.queryVoter(document);
            System.out.println("Resultado de la consulta: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("Error procesando consulta: " + e.getMessage());
            throw new RuntimeException("Error procesando consulta: " + e.getMessage());
        }
    }
}
