package MesaVotacion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import com.zeroc.IceGrid.*;
import com.zeroc.Ice.*;
import votacionRM.*;
import consultaVotacion.*;
import VotacionTest.*;

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

    // Variables para exponer tests de ote station
    private VoteStationImp voteStationServant; // Tu servant
    private ObjectAdapter voteStationAdapter;

    private final String VOTE_STATION_IDENTITY = "VoteStation_Mesa";

    private final String ACK_ADAPTER_ENDPOINT = "default -p ";
    private final String ACK_SERVICE_IDENTITY = "ACKVotoService_Mesa";
    private static int ACK_ADAPTER_PUERTO;

    private static final String MESA_ID_FILE = "id.mesa";
    private static int MESA_ID;

    // query station

    private static final String PUERTO_FILE = "puerto.mesa"; // Archivo para leer el puerto
    private static int PUERTO_MESA; // Variable para almacenar el puerto leído
    private MedidorVoto medidorVoto;

    public Controlador() {
        this.csvManager = new CsvManager(); // Inicializar CsvManager
        this.candidatosCache = null;
        try {
            MESA_ID = leerMesaIdDesdeArchivo();
            PUERTO_MESA = leerPuertoDesdeArchivo();
            ACK_ADAPTER_PUERTO = PUERTO_MESA + 1;
        } catch (java.lang.Exception e) {
            System.out.println("Error al leer mesa");
        }

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
            queryStation = consultaVotacion.queryStationPrx
                    .checkedCast(query.findObjectByType("::consultaVotacion::queryStation"));
            if (queryStation == null) {
                throw new java.lang.Exception("No se pudo obtener el proxy para 'queryStation'");
            }
            System.out.println("Proxy de QueryStation obtenido correctamente");

            if (elecciones == null) {
                throw new java.lang.Exception("No se pudo obtener el proxy para 'elecciones'");
            }
            System.out.println("Iniciando comunicacion confiable");
            inicializarComunicacionConfiable();
            exponerServicioVoteStation();
            this.medidorVoto = new MedidorVoto();

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

            centralizadorRM = votacionRM.CentralizadorRMPrx
                    .checkedCast(query.findObjectByType("::votacionRM::CentralizadorRM"));
            System.out.println("Proxy de Elecciones casteado correctamente");

            if (centralizadorRM == null) {
                throw new java.lang.Exception(
                        "No se pudo conectar al CentralizadorRM en el Servidor Local. Verifique la IP, puerto y que el servicio esté activo.");
            }
            System.out.println("✅ Conectado al Servidor Local ");

            ackService = new ACKVotoServiceI(csvManager);

            com.zeroc.Ice.ObjectAdapter ackAdapter = communicator.createObjectAdapterWithEndpoints("ACKAdapter",
                    ACK_ADAPTER_ENDPOINT + ACK_ADAPTER_PUERTO);

            ackAdapter.add(ackService, com.zeroc.Ice.Util.stringToIdentity(ACK_SERVICE_IDENTITY));

            ackAdapter.activate();
            System.out.println("Mesa de Votación escuchando ACKs en el puerto" + ACK_ADAPTER_PUERTO);
            com.zeroc.Ice.ObjectPrx ackBase = ackAdapter
                    .createProxy(com.zeroc.Ice.Util.stringToIdentity(ACK_SERVICE_IDENTITY));
            ackProxy = votacionRM.ACKVotoServicePrx.checkedCast(ackBase);

            if (ackProxy == null) {
                throw new java.lang.Exception(
                        "No se pudo crear el proxy ACK local. Verifique la identidad '" + ACK_SERVICE_IDENTITY + "'.");
            }
            System.out.println("Proxy local de ACK creado.");

            votosRMTask = new VotosRMTask(csvManager, centralizadorRM, ackProxy, communicator, this);
            votosRMTask.start();

            System.out.println("Comunicación confiable inicializada exitosamente.");

        } catch (com.zeroc.Ice.LocalException iceEx) {
            System.err.println(
                    "Error de comunicación Ice al inicializar (Comunicación Confiable): " + iceEx.getMessage());
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
                throw new java.lang.Exception(
                        "No se puede obtener el listado de candidatos. El servidor no se encuentra disponible");
            }
        }
        try {
            System.out.println("Obteniendo candidatos desde el servidor.");
            candidatosCache = elecciones.obtenerCandidatos();
            return candidatosCache;
        } catch (java.lang.Exception e) {
            throw new java.lang.Exception(
                    "No se pudo obtener la lista de candidatos. Intente mas tarde: " + e.getMessage(), e);
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

        boolean esMesaDeCiudadano = esMesa(personaId);
        if (!esMesaDeCiudadano) {
            boolean cedulaEstaRegistrada = cedulaEstaRegistrada(personaId);

            if (!cedulaEstaRegistrada) {// no esta en la base de datos
                throw new java.lang.Exception("La persona con ID '" + personaId + " No esta registrada para votar");
            } else {
                // Esta en la base de datos pero no en su mesa
                throw new java.lang.Exception(
                        "La persona con ID '" + personaId + " No se encuentra en la mesa que le corresponde");
            }

        }

        try {
            VotoPendiente votoPendiente = new VotoPendiente(mesaId, candidatoId);
            csvManager.agregarVotoPendiente(votoPendiente);
            csvManager.registrarVotante(personaId);
            System.out.println("Voto de " + personaId + " para mesa " + mesaId + " registrado localmente (ID Voto: "
                    + votoPendiente.getId() + ").");

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
                System.out.println("Enviando voto ID: " + voto.getId() + " (Mesa: " + voto.getMesaId() + ")" + ", "
                        + "Candidato: " + voto.getCandidatoId() + ")");
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

    public void obtenerCiudadanosMesa(int mesaId) throws java.lang.Exception {
        if (mesaId <= 0) {
            throw new IllegalArgumentException("El ID de la mesa debe ser un número positivo");
        }

        if (queryStation == null) {
            throw new java.lang.Exception("La conexión con el servicio de consulta no está inicializada.");
        }

        try {
            System.out.println("Obteniendo ciudadanos para la mesa: " + mesaId);
            String[] ciudadanos = queryStation.obtenerCiudadanos(mesaId);

            if (ciudadanos != null && ciudadanos.length > 0) {
                // Crear archivo CSV con los ciudadanos
                String csvFileName = "ciudadanos_mesa.csv";

                try {
                    csvManager.crearCsvCiudadanos(csvFileName, ciudadanos);
                    System.out.println("✅ Archivo CSV creado exitosamente: " + csvFileName);
                    csvManager.inicializarYcargarCiudadanos();
                } catch (IOException e) {
                    System.err.println("❌ Error al crear archivo CSV: " + e.getMessage());
                    throw new java.lang.Exception("Error al crear archivo CSV: " + e.getMessage(), e);
                }
            } else {
                System.out.println("ℹ️ No se encontraron ciudadanos para la mesa: " + mesaId);
            }

        } catch (com.zeroc.Ice.Exception iceEx) {
            System.err.println("❌ Error de ICE al obtener ciudadanos: " + iceEx.getMessage());
            throw new java.lang.Exception("Error al obtener ciudadanos: " + iceEx.getMessage(), iceEx);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Error de validación: " + e.getMessage());
            throw e;
        } catch (java.lang.Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            throw e;
        }
    }

    public void exponerServicioVoteStation() throws java.lang.Exception {
        if (communicator == null) {
            throw new IllegalStateException("ICE communicator is not initialized or is destroyed.");
        }

        System.out.println("Exposing VoteStation service...");

        String voteStationAdapterEndpoint = "default -p " + PUERTO_MESA; // Usar el puerto leído
        voteStationAdapter = communicator.createObjectAdapterWithEndpoints(
                "VoteStationAdapter", voteStationAdapterEndpoint);

        this.voteStationServant = new VoteStationImp(this);
        voteStationAdapter.add(this.voteStationServant, Util.stringToIdentity(VOTE_STATION_IDENTITY));

        voteStationAdapter.activate();
        System.out.println("VoteStation service exposed on port " + PUERTO_MESA
                + " with identity '" + VOTE_STATION_IDENTITY + "'.");
    }

    public int votarTest(String document, int candidateId) {
        System.out.println("Llamado a votar test en controlador");
        if (validarHaVotado(document)) {
            return 2; // Ya votó
        }

        boolean esMesaDeCiudadano = esMesa(document);
        if (!esMesaDeCiudadano) {
            boolean cedulaEstaRegistrada = cedulaEstaRegistrada(document);
            if (!cedulaEstaRegistrada) {
                return 3;
            } else {
                return 1;
            }

        }

        try {
            System.out.println("REGISTRANDO VOTANTE");
            try {
                
            } catch (java.lang.Exception e) {
                e.printStackTrace();
            }

            System.out.println("VOTANTE REGISTRADO");
            System.out.println("Creando voto...");
            votacionRM.Voto voto = new votacionRM.Voto(document, candidateId, MESA_ID);
            System.out.println("Voto creado...");

            try {
                // long tiempo = MedidorVoto.medirTiempoEnvioVoto(voto, centralizadorRM,
                // ackProxy);
                // System.out.println(tiempo);
                //
                try {
                    VotoPendiente votoPendiente = new VotoPendiente(MESA_ID, candidateId);
                    csvManager.agregarVotoPendiente(votoPendiente);
                    csvManager.registrarVotante(document);
                    System.out.println(
                            "Voto de " + document + " para mesa " + MESA_ID + " registrado localmente (ID Voto: "
                                    + votoPendiente.getId() + ").");

                } catch (IOException e) {
                    throw new java.lang.Exception("Error al registrar el voto localmente: " + e.getMessage(), e);
                }
                //
            } catch (java.lang.Exception e) {

                e.printStackTrace();
            }

            return 0; // Voto recibido correctamente

        } catch (com.zeroc.Ice.Exception e) {
            System.err.println("❌ Error al enviar voto directamente: " + e.getMessage());
            e.printStackTrace(); // Para ver la traza completa del error
            return -1; // Error de envío genérico
        }
    }

    public boolean esMesa(String document) {
        return csvManager.esMesaDeCiudadano(document);
    }

    private static int leerMesaIdDesdeArchivo() throws IOException, NumberFormatException {
        int mesaId = -1;
        try (BufferedReader reader = new BufferedReader(new FileReader(MESA_ID_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                mesaId = Integer.parseInt(line.trim());
            } else {

                throw new IOException("El archivo '" + MESA_ID_FILE + "' está vacío.");
            }
        }
        return mesaId;
    }

    private boolean cedulaEstaRegistrada(String document) {
        System.out.println("VERIFICANDO SI LA CEDULA ESTA REGISTRADA....");
        String answer = queryStation.query(document);
        System.out.println("Verficacion completa....");
        System.out.println(answer);
        return !answer.equals("No está registrado");
    }

    private static int leerPuertoDesdeArchivo() throws IOException, NumberFormatException {
        int puerto = -1;
        try (BufferedReader reader = new BufferedReader(new FileReader(PUERTO_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                puerto = Integer.parseInt(line.trim());
            } else {
                throw new IOException("El archivo '" + PUERTO_FILE + "' está vacío.");
            }
        }
        return puerto;
    }

}