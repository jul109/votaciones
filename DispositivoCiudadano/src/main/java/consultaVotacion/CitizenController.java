package consultaVotacion;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import com.zeroc.Ice.Exception;
import consultaVotacion.queryStationPrx;
import com.zeroc.IceGrid.*;

public class CitizenController {

    private com.zeroc.Ice.Communicator communicator;
    private consultaVotacion.queryStationPrx queryStation;

    public CitizenController() {
        try {
            String[] args = {};
            communicator = com.zeroc.Ice.Util.initialize(args, "config.client");
            System.out.println("conectando con el broker");

            com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("DemoIceGrid/Query");
            System.out.println("Proxy de Consulta obtenido");

            QueryPrx query = QueryPrx.checkedCast(base);
            System.out.println("Proxy de Query casteado correctamente");

            queryStation = consultaVotacion.queryStationPrx.checkedCast(query.findObjectByType("::consultaVotacion::queryStation"));
            System.out.println("Proxy de QueryStation casteado correctamente");

            if (queryStation == null) {
                throw new RuntimeException("Error: Invalid proxy");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing controller: " + e.getMessage());
        }
    }

    public String queryDocument(String document) {
        try {
            return queryStation.query(document);
        } catch (Exception e) {
            throw new RuntimeException("Error querying document: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (communicator != null) {
            try {
                communicator.destroy();
            } catch (Exception e) {
                System.err.println("Error shutting down: " + e.getMessage());
            }
        }
    }
}
