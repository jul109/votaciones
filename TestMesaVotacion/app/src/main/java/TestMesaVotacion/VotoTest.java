package TestMesaVotacion;

public class VotoTest {

    private String documentoVotante;
    private int idCandidato; 
    private String ipMesa;
    private String puerto; // Nuevo atributo añadido

    public VotoTest(String documentoVotante, int idCandidato, String ipMesa, String puerto) {
        this.documentoVotante = documentoVotante;
        this.idCandidato = idCandidato;
        this.ipMesa = ipMesa;
        this.puerto = puerto; // Inicializar el nuevo atributo
    }

    public String getDocumentoVotante() {
        return documentoVotante;
    }

    public int getIdCandidato() {
        return idCandidato;
    }

    public String getIpMesa() {
        return ipMesa;
    }

    public String getPuerto() { // Getter para el nuevo atributo
        return puerto;
    }

    public void setDocumentoVotante(String documentoVotante) {
        this.documentoVotante = documentoVotante;
    }

    public void setIdCandidato(int idCandidato) {
        this.idCandidato = idCandidato;
    }

    public void setIpMesa(String ipMesa) {
        this.ipMesa = ipMesa;
    }

    public void setPuerto(String puerto) { // Setter para el nuevo atributo
        this.puerto = puerto;
    }

    @Override
    public String toString() {
        return "VotoTest{" +
               "documentoVotante='" + documentoVotante + '\'' +
               ", idCandidato=" + idCandidato +
               ", ipMesa='" + ipMesa + '\'' +
               ", puerto='" + puerto + '\'' + // Incluido el nuevo atributo en toString
               '}';
    }
}