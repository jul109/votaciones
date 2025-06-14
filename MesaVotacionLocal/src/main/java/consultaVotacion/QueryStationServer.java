package consultaVotacion;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import com.zeroc.Ice.Exception;
import com.zeroc.Ice.Current;
import consultaVotacion.*;
import com.zeroc.Ice.*;
import com.zeroc.IceGrid.*;
import java.sql.*;


public class QueryStationServer {

    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            // Crear el administrador de base de datos
            DatabaseManager dbManager = new DatabaseManager();

            // Crear el adaptador de objetos
            ObjectAdapter adapter = communicator.createObjectAdapter("MesaLocalAdapter");

            // Crear e instanciar el servidor
            queryStationI queryStationServant = new queryStationI(dbManager);
            Properties properties = communicator.getProperties();
            Identity id = Util.stringToIdentity(properties.getProperty("Identity"));
            adapter.add(queryStationServant, id);

            // Activar el adaptador
            adapter.activate();

            System.out.println("Servidor iniciado en el puerto 10000 ");

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
            if (result == null) {
                System.out.println("Documento no encontrado: " + document);
                return "No está registrado";
            }
            System.out.println("Resultado de la consulta: " + result + "\n\n");
            return result;
        } catch (Exception e) {
            System.err.println("Error procesando consulta: " + e.getMessage());
            throw new RuntimeException("Error procesando consulta: " + e.getMessage());
        }
    }

    @Override
    public String[] obtenerCiudadanos(String mesaId, Current current) {
        System.out.println("Consulta recibida para obtener ciudadanos de la mesa: " + mesaId);
        try {
            String[] result = dbManager.obtenerCiudadanos(mesaId);
            System.out.println("Se encontraron " + result.length + " ciudadanos para la mesa " + mesaId);
            return result;
        } catch (Exception e) {
            System.err.println("Error obteniendo ciudadanos: " + e.getMessage());
            throw new RuntimeException("Error obteniendo ciudadanos: " + e.getMessage());
        }
    }
}
