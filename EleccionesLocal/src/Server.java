package EleccionesLocal;

import com.zeroc.Ice.*;
import Votacion.*;

public class Server {
    public static void main(String[] args) {
        int status = 0;
        try (Communicator communicator = Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("EleccionesAdapter", "default -p 10009");
            adapter.add(new EleccionesI(), Util.stringToIdentity("Elecciones"));
            adapter.activate();
            System.out.println("Servidor Elecciones Local activo en el puerto 10009...");
            communicator.waitForShutdown();
        } catch (Exception e) {
            e.printStackTrace();
            status = 1;
        }
        System.exit(status);
    }
}
