package MesaVotacion;

import java.io.IOException;
import java.util.List;
import com.zeroc.IceGrid.*;
import com.zeroc.Ice.*;
import votacionRM.*;
import consultaVotacion.*;

public class Controlador {
    private com.zeroc.Ice.Communicator communicator;
    private Votacion.EleccionesPrx elecciones;
    private CsvManager csvManager;
    private Votacion.Candidato[] candidatosCache;
    private consultaVotacion.queryStationPrx queryStation;

    // Variables para comunicación confiable
    private votacionRM.CentralizadorRMPrx centralizadorRM;
    private ACKVotoServiceI ackService;
    private votacionRM.ACKVotoServicePrx ackProxy;
    private VotosRMTask votosRMTask;


    private final String ACK_ADAPTER_ENDPOINT = "default -p 10014";
    private final String ACK_SERVICE_IDENTITY = "ACKVotoService_Mesa";

    public Controlador() {
        this.csvManager = new CsvManager(); // Inicializar CsvManager
        this.candidatosCache = null;
    }

    public void inicializarConexion() throws java.lang.Exception {
        try {
            String[] args = {};
            communicator = com.zeroc.Ice.Util.initialize(args, "config.client");
            System.out.println("conectando con el broker");

            com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("DemoIceGrid/Query");
            System.out.println("Proxy de Elecciones obtenido");

            QueryPrx query = QueryPrx.checkedCast(base);
            System.out.println("Proxy de Query casteado correctamente");

            elecciones = Votacion.EleccionesPrx.checkedCast(query.findObjectByType("::Votacion::Elecciones"));
            System.out.println("Proxy de Elecciones casteado correctamente");

            // Obtener el proxy para el servicio de consulta de mesa local
            queryStation = consultaVotacion.queryStationPrx.checkedCast(query.findObjectByType("::consultaVotacion::queryStation"));
            if (queryStation == null) {
                throw new java.lang.Exception("No se pudo obtener el proxy para 'queryStation'");
            }
            System.out.println("Proxy de QueryStation obtenido correctamente");

            if (elecciones == null) {
                throw new java.lang.Exception("No se pudo obtener el proxy para 'elecciones'");
            }
            System.out.println("Iniciando comunicacion confiable");
            inicializarComunicacionConfiable();

        } catch (java.lang.Exception e) {
            e.printStackTrace();
            throw new java.lang.Exception("Error al inicializar la conexión con el servidor ICE: " + e.getMessage(), e);
        }
    }

    public void inicializarComunicacionConfiable() throws java.lang.Exception {
        try {
            System.out.println("Inicializando comunicación confiable...");

            String[] args = {};
            communicator = com.zeroc.Ice.Util.initialize(args, "config.client");
            System.out.println("conectando con el broker");
            
            com.zeroc.Ice.ObjectPrx baseCentralizador = communicator.stringToProxy("DemoIceGrid/Query");

            QueryPrx query = QueryPrx.checkedCast(baseCentralizador);
            System.out.println("Proxy de Query casteado correctamente");

            centralizadorRM = votacionRM.CentralizadorRMPrx.checkedCast(query.findObjectByType("::votacionRM::CentralizadorRM"));
            System.out.println("Proxy de Elecciones casteado correctamente");

            if (centralizadorRM == null) {
                throw new java.lang.Exception("No se pudo conectar al CentralizadorRM en el Servidor Local. Verifique la IP, puerto y que el servicio esté activo.");
            }
            System.out.println("✅ Conectado al Servidor Local ");

            ackService = new ACKVotoServiceI(csvManager);
            
            com.zeroc.Ice.ObjectAdapter ackAdapter = communicator.createObjectAdapterWithEndpoints("ACKAdapter", ACK_ADAPTER_ENDPOINT);
            
            ackAdapter.add(ackService, com.zeroc.Ice.Util.stringToIdentity(ACK_SERVICE_IDENTITY));
            
            ackAdapter.activate();
            System.out.println("Mesa de Votación escuchando ACKs en el puerto " + ACK_ADAPTER_ENDPOINT.split("-p ")[1] + ".");

            com.zeroc.Ice.ObjectPrx ackBase = ackAdapter.createProxy(com.zeroc.Ice.Util.stringToIdentity(ACK_SERVICE_IDENTITY));
            ackProxy = votacionRM.ACKVotoServicePrx.checkedCast(ackBase);

            if (ackProxy == null) {
                throw new java.lang.Exception("No se pudo crear el proxy ACK local. Verifique la identidad '" + ACK_SERVICE_IDENTITY + "'.");
            }
            System.out.println("Proxy local de ACK creado.");

            votosRMTask = new VotosRMTask(csvManager, centralizadorRM, ackProxy, communicator, this);
            votosRMTask.start();

            System.out.println("Comunicación confiable inicializada exitosamente.");

        } catch (com.zeroc.Ice.LocalException iceEx) {
            System.err.println("Error de comunicación Ice al inicializar (Comunicación Confiable): " + iceEx.getMessage());
            iceEx.printStackTrace();
            throw iceEx;
        } catch (java.lang.Exception rmEx) {
            System.err.println(" Error general al inicializar comunicación confiable: " + rmEx.getMessage());
            rmEx.printStackTrace();
            throw rmEx;
        }
    }

    public Votacion.Candidato[] obtenerCandidatos() throws java.lang.Exception {
        if (candidatosCache != null) {
            System.out.println("Obteniendo candidatos desde la caché.");
            return candidatosCache;
        }
        if (elecciones == null) {
            try {
                inicializarConexion();
            } catch (java.lang.Exception e) {
                throw new java.lang.Exception("No se puede obtener el listado de candidatos. El servidor no se encuentra disponible");
            }
        }
        try {
            System.out.println("Obteniendo candidatos desde el servidor.");
            candidatosCache = elecciones.obtenerCandidatos();
            return candidatosCache;
        } catch (java.lang.Exception e) {
            throw new java.lang.Exception("No se pudo obtener la lista de candidatos. Intente mas tarde: " + e.getMessage(), e);
        }
    }

    public boolean validarHaVotado(String personaId) {
        if (personaId == null || personaId.trim().isEmpty()) {
            System.err.println("validarHaVotado: personaId no puede ser nulo o vacío.");
            return false;
        }
        return csvManager.haVotado(personaId);
    }

    public void votar(String personaId, int mesaId, int candidatoId) throws java.lang.Exception {
        if (personaId == null || personaId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la persona no puede ser nulo o vacío.");
        }
        if (validarHaVotado(personaId)) {
            throw new java.lang.Exception("La persona con ID '" + personaId + "' ya ha emitido su voto.");
        }

        try {
            VotoPendiente votoPendiente = new VotoPendiente(mesaId, candidatoId);
            csvManager.agregarVotoPendiente(votoPendiente);
            csvManager.registrarVotante(personaId);
            System.out.println("Voto de " + personaId + " para mesa " + mesaId +  " registrado localmente (ID Voto: " + votoPendiente.getId() + ").");

        } catch (IOException e) {
            throw new java.lang.Exception("Error al registrar el voto localmente: " + e.getMessage(), e);
        }
    }

    public void enviarVotosPendientesAlServidor() throws java.lang.Exception {
        if (elecciones == null) {
            throw new java.lang.Exception("La conexión con el servidor ICE no está inicializada.");
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
                System.out.println("Enviando voto ID: " + voto.getId() + " (Mesa: " + voto.getMesaId() + ")" + ", " + "Candidato: " + voto.getCandidatoId() + ")");
                elecciones.registrarVoto(voto.getCandidatoId(), voto.getMesaId());
                System.out.println("Voto ID: " + voto.getId() + " enviado con éxito al servidor.");

                csvManager.eliminarVotoPendiente(voto.getId());
                votosEnviadosConExito++;
            } catch (com.zeroc.Ice.Exception iceEx) {
                System.err.println("Error de ICE al enviar voto ID " + voto.getId() + ": " + iceEx.getMessage());
                idsVotosFallidos.add(voto.getId());
            } catch (IOException csvEx) {
                System.err.println("Error de CSV al intentar eliminar voto ID " + voto.getId()
                        + " después de un envío (potencialmente) exitoso: " + csvEx.getMessage());
                idsVotosFallidos.add(voto.getId());
            } catch (java.lang.Exception e) {
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
            } catch (java.lang.Exception e) {
                System.err.println("Error al cerrar la conexión ICE: " + e.getMessage());
            }
        }
    }

    public void obtenerCiudadanosMesa(String mesaId) throws java.lang.Exception {
        if (queryStation == null) {
            throw new java.lang.Exception("La conexión con el servicio de consulta no está inicializada.");
        }

        try {
            System.out.println("Obteniendo ciudadanos para la mesa: " + mesaId);
            String[] ciudadanos = queryStation.obtenerCiudadanos(mesaId);
            
            if (ciudadanos != null && ciudadanos.length > 0) {
                // Crear archivo CSV con los ciudadanos
                String csvFileName = "ciudadanos_mesa_" + mesaId + ".csv";
                csvManager.crearCsvCiudadanos(csvFileName, ciudadanos);
                System.out.println("Archivo CSV creado exitosamente: " + csvFileName);
            } else {
                System.out.println("No se encontraron ciudadanos para la mesa: " + mesaId);
            }
        } catch (com.zeroc.Ice.Exception iceEx) {
            System.err.println("Error de ICE al obtener ciudadanos: " + iceEx.getMessage());
            throw new java.lang.Exception("Error al obtener ciudadanos: " + iceEx.getMessage(), iceEx);
        } catch (IOException e) {
            System.err.println("Error al crear archivo CSV: " + e.getMessage());
            throw new java.lang.Exception("Error al crear archivo CSV: " + e.getMessage(), e);
        }
    }
}