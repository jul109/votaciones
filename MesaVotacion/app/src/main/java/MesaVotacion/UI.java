package MesaVotacion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class UI {
    private Controlador controlador;
    private Scanner scanner;
    private static final String MESA_ID_FILE = "id.mesa";
    private static int MESA_ID;

    public UI(Controlador controlador) {
        this.controlador = controlador;

        this.scanner = new Scanner(System.in);

        try {
            MESA_ID = leerMesaIdDesdeArchivo();
            System.out.println("ID de Mesa cargado: " + MESA_ID);
        } catch (IOException e) {
            System.out.println(
                    "Error crítico: No se pudo leer el ID de la mesa desde '" + MESA_ID_FILE + "'. " + e.getMessage());
            MESA_ID = -1;
        } catch (NumberFormatException e) {
            System.out.println("Error crítico: El contenido del archivo '" + MESA_ID_FILE + "' no es un número válido. "
                    + e.getMessage());
            MESA_ID = -1;
        }
    }

    public void menu() {
        try {
            controlador.inicializarConexion();

            try {
                controlador.obtenerCiudadanosMesa(MESA_ID);
            } catch (Exception e) {
                System.out.println("Error al obtener ciudadanos de la mesa: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Error No se puede iniciar conexion con el Servidor. Las funcionalidades son limitadas" + e.getMessage());
        }

        while (true) {
            System.out.println("\n--- Menú ---");
            System.out.println("1. Votar");
            System.out.println("2. Salir");
            System.out.print("Seleccione una opción: \n");

            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    votar();
                    break;
                case 2:
                    System.out.println("Saliendo...");
                    return;
                default:
                    System.out.println("Opción inválida.");
            }
        }

    }

    private void mostrarCandidatos(Votacion.Candidato[] candidatos) {
        System.out.println("\n--- Lista de Candidatos ---");
        for (Votacion.Candidato candidato : candidatos) {
            System.out.println(
                    "ID: " + candidato.id + ", Nombre: " + candidato.nombre + ", Partido: " + candidato.partido);
        }
    }

    private void votar() {
        System.out.println("Escriba su ID ");
        Votacion.Candidato[] candidatos = null;
        try {
            candidatos = controlador.obtenerCandidatos();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (candidatos == null || candidatos.length == 0) {
            System.out.println("No hay candidatos disponibles para votar.");
            return;
        }

        votarConCandidatos(candidatos);
    }

    private void votarConCandidatos(Votacion.Candidato[] candidatos) {
        System.out.println("Ingrese su ID");
        String id = scanner.next();
        if (controlador.validarHaVotado(id)) {
            System.out.println("Usted ya voto");
            return;
        }
        mostrarCandidatos(candidatos);

        System.out.print("Ingrese el ID del candidato: \n");
        int candidatoId = scanner.nextInt();
        scanner.nextLine();
        try {
            controlador.votar(id, MESA_ID, candidatoId);
            System.out.println("Voto registrado localmente de forma exitosa");

        } catch (Exception e) {
            System.out.println("Error al registrar el voto: " + e.getMessage());
        }
    }

    private static int leerMesaIdDesdeArchivo() throws IOException, NumberFormatException {
        int mesaId = -1;
        try (BufferedReader reader = new BufferedReader(new FileReader(MESA_ID_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                mesaId = Integer.parseInt(line.trim()); // Converts the line to an integer
            } else {

                throw new IOException("El archivo '" + MESA_ID_FILE + "' está vacío.");
            }
        }
        return mesaId;
    }

}
