/*
 * This Java source file was generated by the Gradle 'init' task.
 */


package MesaVotacion;

public class MesaVotacion {
    public static void main(String[] args) {
        Controlador controlador = new Controlador();
        try {
            
            UI ui = new UI(controlador);
            ui.menu();
            return;
        } catch (Exception e) {
            System.out.println("Error al inicializar la aplicación: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }
}
