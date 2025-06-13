package consultaVotacion;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import com.zeroc.Ice.Exception;
import consultaVotacion.queryStationPrx;

public class CitizenController {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 10000;
    private static final String ENDPOINT = "tcp -h " + SERVER_IP + " -p " + SERVER_PORT;

    private Communicator communicator;
    private queryStationPrx queryStation;

    public CitizenController() {
        try {
            communicator = Util.initialize();
            ObjectPrx base = communicator.stringToProxy("QueryStation:" + ENDPOINT);
            queryStation = queryStationPrx.checkedCast(base);

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
