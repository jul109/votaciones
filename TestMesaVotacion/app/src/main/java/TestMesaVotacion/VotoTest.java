package TestMesaVotacion;

public class VotoTest {

    private String documentoVotante;
    private int idCandidato; 
    private String candidatoSeleccionadoNombre; 
    private String ipMesa;

    public VotoTest(String documentoVotante, int idCandidato, String candidatoSeleccionadoNombre, String ipMesa) {
        this.documentoVotante = documentoVotante;
        this.idCandidato = idCandidato;
        this.candidatoSeleccionadoNombre = candidatoSeleccionadoNombre;
        this.ipMesa = ipMesa;
    }

    public String getDocumentoVotante() {
        return documentoVotante;
    }

    public int getIdCandidato() {
        return idCandidato;
    }

    public String getCandidatoSeleccionadoNombre() {
        return candidatoSeleccionadoNombre;
    }

    public String getIpMesa() {
        return ipMesa;
    }

    public void setDocumentoVotante(String documentoVotante) {
        this.documentoVotante = documentoVotante;
    }

    public void setIdCandidato(int idCandidato) {
        this.idCandidato = idCandidato;
    }

    public void setCandidatoSeleccionadoNombre(String candidatoSeleccionadoNombre) {
        this.candidatoSeleccionadoNombre = candidatoSeleccionadoNombre;
    }

    public void setIpMesa(String ipMesa) {
        this.ipMesa = ipMesa;
    }

    @Override
    public String toString() {
        return "VotoTest{" +
               "documentoVotante='" + documentoVotante + '\'' +
               ", idCandidato=" + idCandidato +
               ", candidatoSeleccionadoNombre='" + candidatoSeleccionadoNombre + '\'' +
               ", ipMesa='" + ipMesa + '\'' +
               '}';
    }
}