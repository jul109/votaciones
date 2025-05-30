package MesaVotacion;

import java.io.IOException;
import java.util.List;
import com.zeroc.IceGrid.*;

public class Controlador {
    private com.zeroc.Ice.Communicator communicator;
    private Votacion.EleccionesPrx elecciones;
    private CsvManager csvManager;
    private Votacion.Candidato[] candidatosCache;

    public Controlador() {
        this.csvManager = new CsvManager(); // Inicializar CsvManager
        this.candidatosCache = null;
    }

    public void inicializarConexion() throws Exception {
        try {
            /*
            communicator = com.zeroc.Ice.Util.initialize();
            com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("Elecciones:default -h x206m02 -p 10009");
            elecciones = Votacion.EleccionesPrx.checkedCast(base);
            if (elecciones == null) {
                throw new Exception(
                        "No se pudo obtener el proxy de Elecciones. Verifica la conexión y el nombre del proxy.");
            }
             */

            String[] args = {};
            communicator = com.zeroc.Ice.Util.initialize(args, "config.client");
            System.out.println("conectando con el broker");

            com.zeroc.Ice.ObjectPrx base = communicator.propertyToProxy("Ice.Default.Locator");
            System.out.println("Proxy de Elecciones obtenido");

            QueryPrx query = QueryPrx.checkedCast(base);
            System.out.println("Proxy de Query casteado correctamente");

            elecciones = Votacion.EleccionesPrx.checkedCast(query.findObjectByType("::Votacion::Elecciones"));
            System.out.println("Proxy de Elecciones casteado correctamente");

            if (elecciones == null) {
                throw new Exception("No se pudo obtener el proxy para 'elecciones'");
            }
        } catch (Exception e) {
            throw new Exception("Error al inicializar la conexión con el servidor ICE: " + e.getMessage(), e);
        }
    }

    public Votacion.Candidato[] obtenerCandidatos() throws Exception {
        if (candidatosCache != null) {
            System.out.println("Obteniendo candidatos desde la caché.");
            return candidatosCache;
        }
        if (elecciones == null) {
            try {
                inicializarConexion();
            } catch (Exception e) {
                throw new Exception("No se puede obtener el listado de candidatos. El servidor no se encuentra disponible");
            }
            
        }
        try {

            System.out.println("Obteniendo candidatos desde el servidor.");
            candidatosCache = elecciones.obtenerCandidatos();
            return candidatosCache;
        } catch (Exception e) {
            throw new Exception("No se pudo obtener la lista de candidatos. Intente mas tarde: " + e.getMessage(), e);
        }
    }

    public boolean validarHaVotado(String personaId) {
        if (personaId == null || personaId.trim().isEmpty()) {
            System.err.println("validarHaVotado: personaId no puede ser nulo o vacío.");
            return false;
        }
        return csvManager.haVotado(personaId);
    }

    public void votar(String personaId, int mesaId, int candidatoId) throws Exception {
        if (personaId == null || personaId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la persona no puede ser nulo o vacío.");
        }
        if (validarHaVotado(personaId)) {
            throw new Exception("La persona con ID '" + personaId + "' ya ha emitido su voto.");
        }

        try {
            VotoPendiente votoPendiente = new VotoPendiente(mesaId, candidatoId);
            csvManager.agregarVotoPendiente(votoPendiente);
            csvManager.registrarVotante(personaId);
            System.out.println("Voto de " + personaId + " para mesa " + mesaId +  " registrado localmente (ID Voto: " + votoPendiente.getId() + ").");

        } catch (IOException e) {

            throw new Exception("Error al registrar el voto localmente: " + e.getMessage(), e);
        }
    }

    /**
     * Envía todos los votos pendientes almacenados localmente al servidor Ice.
     * Si un voto se envía con éxito, se elimina de la lista local de pendientes.
     * 
     * @throws Exception Si no se puede conectar al servidor Ice o si hay errores
     *                   durante el envío.
     */
    public void enviarVotosPendientesAlServidor() throws Exception {
        if (elecciones == null) {
            throw new Exception("La conexión con el servidor ICE no está inicializada.");
        }

        List<VotoPendiente> pendientes = csvManager.getVotosPendientes();
        if (pendientes.isEmpty()) {
            System.out.println("No hay votos pendientes para enviar al servidor.");
            return;
        }

        System.out.println("Intentando enviar " + pendientes.size() + " votos pendientes al servidor...");
        int votosEnviadosConExito = 0;
        List<String> idsVotosFallidos = new java.util.ArrayList<>();

        for (VotoPendiente voto : pendientes) {
            try {
                System.out.println("Enviando voto ID: " + voto.getId() + " (Mesa: " + voto.getMesaId() + ")");
                elecciones.registrarVoto(voto.getMesaId(), voto.getCandidatoId()); // Llamada al servidor Ice
                System.out.println("Voto ID: " + voto.getId() + " enviado con éxito al servidor.");

                // Si el envío fue exitoso, eliminarlo de la lista local
                csvManager.eliminarVotoPendiente(voto.getId());
                votosEnviadosConExito++;
            } catch (com.zeroc.Ice.Exception iceEx) {
                System.err.println("Error de ICE al enviar voto ID " + voto.getId() + ": " + iceEx.getMessage());
                idsVotosFallidos.add(voto.getId());
            } catch (IOException csvEx) {
                System.err.println("Error de CSV al intentar eliminar voto ID " + voto.getId()
                        + " después de un envío (potencialmente) exitoso: " + csvEx.getMessage());
    
                idsVotosFallidos.add(voto.getId()); // Marcar como fallido para revisión
            } catch (Exception e) {
                System.err.println("Error general al procesar voto ID " + voto.getId() + ": " + e.getMessage());
                idsVotosFallidos.add(voto.getId());
            }
        }
        System.out.println("Proceso de envío completado. Votos enviados con éxito: " + votosEnviadosConExito + "/"
                + pendientes.size());
        if (!idsVotosFallidos.isEmpty()) {
            System.err.println(
                    "Los siguientes IDs de votos no pudieron ser procesados completamente y permanecen como pendientes o requieren revisión: "
                            + idsVotosFallidos);
        }
    }

    public void cerrarConexion() {
        if (communicator != null) {
            try {
                communicator.destroy();
                System.out.println("Conexión ICE cerrada.");
            } catch (Exception e) {
                System.err.println("Error al cerrar la conexión ICE: " + e.getMessage());
            }
        }
    }
}