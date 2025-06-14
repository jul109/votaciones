package consultaVotacion;

import java.util.Scanner;

public class CitizenUI {

    private CitizenController controller;
    private Scanner scanner;

    public CitizenUI() {
        this.controller = new CitizenController();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        try {
            while (true) {
                System.out.println("\n=== Consulta de Votación ===");
                System.out.println("Ingrese el número de cédula (o 'salir' para terminar):");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("salir")) {
                    break;
                }

                try {
                    String response = controller.queryDocument(input);
                    System.out.println("Usted debe votar en " + response + "\n\n");
                } catch (Exception e) {
                    System.err.println("Error al consultar: " + e.getMessage());
                }
            }
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
        if (controller != null) {
            controller.shutdown();
        }
    }

    public static void main(String[] args) {
        CitizenUI ui = new CitizenUI();
        ui.start();
    }
}
