package MesaVotacion;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.*;

public class CsvManager {

    private final Path votosPendientesFilePath;
    private final Path votantesFilePath;

    private final List<VotoPendiente> votosPendientes;
    private final Set<Votante> personasQueVotaron; 
    private Set<String> ciudadanos; 
    private final String nombreArchivoCiudadanos = "ciudadanos_mesa.csv";
    private final Path ciudadanosFilePath = Paths.get(nombreArchivoCiudadanos);;
    

    public CsvManager() {
        String nombreArchivoVotosPendientes = "votos_pendientes.csv";
        String nombreArchivoVotantes = "votantes_registrados.csv";

        this.votosPendientesFilePath = Paths.get(nombreArchivoVotosPendientes);
        this.votantesFilePath = Paths.get(nombreArchivoVotantes);

        this.votosPendientes = Collections.synchronizedList(new ArrayList<>());
        this.personasQueVotaron = Collections.synchronizedSet(new HashSet<>());

        cargarVotosPendientes();
        
        
        
    }

    // --- Gestión de Votos Pendientes ---

    /**
     * Agrega un VotoPendiente existente a la lista y lo persiste en el archivo CSV.
     * Se asume que el VotoPendiente ya tiene su ID único generado.
     * @param votoPendiente El objeto VotoPendiente a agregar.
     * @return El VotoPendiente que fue agregado.
     * @throws IOException Si ocurre un error al escribir en el archivo CSV.
     * @throws IllegalArgumentException Si el votoPendiente es nulo.
     */
    public VotoPendiente agregarVotoPendiente(VotoPendiente votoPendiente) throws IOException {
        if (votoPendiente == null) {
            throw new IllegalArgumentException("El objeto VotoPendiente no puede ser nulo.");
        }
        
        votosPendientes.add(votoPendiente); 
        try {
            
            String csvLine = votoPendiente.toCsvString() + System.lineSeparator();
            Files.writeString(votosPendientesFilePath, csvLine, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            
            votosPendientes.remove(votoPendiente);
            System.err.println("Error al guardar el voto pendiente en CSV: " + votoPendiente.getId() + ". Error: " + e.getMessage());
            throw e; 
        }
        return votoPendiente;
    }


    public void eliminarVotoPendiente(String votoId) throws IOException {
        boolean removed = votosPendientes.removeIf(voto -> voto.getId().equals(votoId));
        if (removed) {
            guardarTodosLosVotosPendientes(); 
        }
    }

    private void cargarVotosPendientes() {
        if (Files.exists(votosPendientesFilePath)) {
            try {
                List<String> lines = Files.readAllLines(votosPendientesFilePath, StandardCharsets.UTF_8);
                
                synchronized (votosPendientes) {
                    votosPendientes.clear();
                    for (String line : lines) {
                        if (line.trim().isEmpty()) continue;
                        try {
                            votosPendientes.add(VotoPendiente.fromCsvString(line));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Error al parsear línea de VotoPendiente CSV (se omitirá): '" + line + "'. Error: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error crítico al cargar votos pendientes desde CSV: " + e.getMessage());
            }
        }
    }

    private void guardarTodosLosVotosPendientes() throws IOException {
        List<VotoPendiente> copiaVotos;
        synchronized (votosPendientes) {
            copiaVotos = new ArrayList<>(votosPendientes);
        }
        
        List<String> lines = copiaVotos.stream()
                                       .map(VotoPendiente::toCsvString)
                                       .collect(Collectors.toList());
        Files.write(votosPendientesFilePath, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public List<VotoPendiente> getVotosPendientes() {
        synchronized (votosPendientes) {
            return new ArrayList<>(votosPendientes);
        }
    }


    public Votante registrarVotante(String personaId) throws IOException {
        if (personaId == null || personaId.trim().isEmpty()) {
            System.err.println("Intento de registrar votante con ID nulo o vacío.");
            return null; 
        }
        Votante nuevoVotante = new Votante(personaId.trim()); // Crear objeto Votante
        
        boolean esNuevo;
        synchronized(personasQueVotaron) { // Sincronizar el acceso al Set
            esNuevo = personasQueVotaron.add(nuevoVotante);
        }

        if (esNuevo) {
            try {
                String csvLine = nuevoVotante.toCsvString() + System.lineSeparator();
                Files.writeString(votantesFilePath, csvLine, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                return nuevoVotante;
            } catch (IOException e) {
                
                synchronized(personasQueVotaron) {
                    personasQueVotaron.remove(nuevoVotante);
                }
                System.err.println("Error al guardar el Votante en CSV: " + nuevoVotante.getId() + ". Error: " + e.getMessage());
                throw e;
            }
        }
        return null; 
    }

    private void cargarVotantes() {
        if (Files.exists(votantesFilePath)) {
            try {
                List<String> lines = Files.readAllLines(votantesFilePath, StandardCharsets.UTF_8);
                synchronized (personasQueVotaron) {
                    personasQueVotaron.clear();
                    for (String line : lines) {
                        if (line.trim().isEmpty()) continue;
                        try {
                            
                            personasQueVotaron.add(Votante.fromCsvString(line));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Error al parsear línea de Votante CSV (se omitirá): '" + line + "'. Error: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error crítico al cargar Votantes desde CSV: " + e.getMessage());
            }
        }
    }

    public boolean haVotado(String personaId) {
        if (personaId == null || personaId.trim().isEmpty()) {
            return false;
        }
        Votante votanteParaBuscar = new Votante(personaId.trim());
        synchronized (personasQueVotaron) { 
            return personasQueVotaron.contains(votanteParaBuscar);
        }
    }

    public Set<Votante> getPersonasQueVotaron() {
        synchronized (personasQueVotaron) {
            return new HashSet<>(personasQueVotaron);
        }
    }

    public void crearCsvCiudadanos(String fileName, String[] ciudadanos) throws IOException {
        try (FileWriter writer = new FileWriter(fileName);
             BufferedWriter bw = new BufferedWriter(writer);
             PrintWriter out = new PrintWriter(bw)) {
            
            // Escribir encabezado
            out.println("Documento");
            
            // Escribir cada ciudadano
            for (String ciudadano : ciudadanos) {
                out.println(ciudadano);
            }
            
        }
    }

    public void inicializarYcargarCiudadanos() {
        System.out.println("Cargandos ciudadanos de la mesa");
        Set<String> tempCiudadanos = Collections.synchronizedSet(new HashSet<>());
        if (Files.exists(ciudadanosFilePath)) {
            try {
                List<String> lines = Files.readAllLines(ciudadanosFilePath, StandardCharsets.UTF_8);
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    tempCiudadanos.add(line.trim()); 
                }
                System.out.println("Set de ciudadanos cargado exitosamente desde: " + nombreArchivoCiudadanos);
            } catch (IOException e) {
                System.err.println("Error crítico al cargar ciudadanos desde " + nombreArchivoCiudadanos + ": " + e.getMessage());
                
            }
        } else {
            System.out.println("El archivo de ciudadanos '" + nombreArchivoCiudadanos + "' no existe. El Set se inicializará vacío.");
        }
        System.out.println("Ciudadanos de la mesa cargados");
        this.ciudadanos=tempCiudadanos;
        
    }
    public boolean esMesaDeCiudadano(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        synchronized (ciudadanos) {
            
            return ciudadanos.contains(id.trim());
        }
    }
}