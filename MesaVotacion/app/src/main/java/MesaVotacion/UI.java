package MesaVotacion;

import java.util.Scanner;

public class UI {
    private Controlador controlador;
    private Scanner scanner;

    public UI(Controlador controlador) {
        this.controlador = controlador;
        this.scanner = new Scanner(System.in);
    }

    public void menu() {
        try {
            controlador.inicializarConexion();
            
        } catch (Exception e) {
            System.out.println("Error No se puede iniciar conexion con el Servidor. Las funcionalidades son limitadas");
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
        String id=scanner.next();
        if(controlador.validarHaVotado(id)){
            System.out.println("Usted ya voto");
            return;
        }
        mostrarCandidatos(candidatos);
        int mesaId = 1;
        System.out.print("Ingrese el ID del candidato: \n");
        int candidatoId = scanner.nextInt();
        scanner.nextLine();
        try {
            controlador.votar(id,mesaId, candidatoId);
            System.out.println("Voto registrado localmente de forma exitosa");
            

        } catch (Exception e) {
            System.out.println("Error al registrar el voto: " + e.getMessage());
        }
    }

}
