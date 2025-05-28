package MesaVotacion;

public class Controlador {
    private com.zeroc.Ice.Communicator communicator;
    private Votacion.EleccionesPrx elecciones;

    public void inicializarConexion() throws Exception {
        try {
            communicator = com.zeroc.Ice.Util.initialize();
            com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("Elecciones:default -h x206m02 -p 10009");
            elecciones = Votacion.EleccionesPrx.checkedCast(base);
            if (elecciones == null) {
                throw new Exception("No se pudo obtener el proxy de Elecciones. Verifica la conexión y el nombre del proxy.");
            }
        } catch (Exception e) {
            throw new Exception("Error al inicializar la conexión con el servidor ICE: " + e.getMessage(), e);
        }
    }

    public void votar(int mesaId, int candidatoId) throws Exception {
        try {
            elecciones.registrarVoto(mesaId, candidatoId);
        } catch (Exception e) {
            throw new Exception("No se pudo registrar el voto para la mesa " + mesaId + " y el candidato " + candidatoId + ": " + e.getMessage(), e);
        }
    }

    public Votacion.Candidato[] obtenerCandidatos() throws Exception {
        try {
            return elecciones.obtenerCandidatos();
        } catch (Exception e) {
            throw new Exception("No se pudo obtener la lista de candidatos. Intente mas tarde" + e.getMessage(), e);
        }
    }
}
