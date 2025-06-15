package TestMesaVotacion;

public class ResultadoVoto {
    private final String documento;
    private final int idCandidato;
    private final String ipMesa;
    private final String puerto;
    private final long tiempoMs;
    private final int respuesta;

    public ResultadoVoto(String documento, int idCandidato, String ipMesa, String puerto, long tiempoMs, int respuesta) {
        this.documento = documento;
        this.idCandidato = idCandidato;
        this.ipMesa = ipMesa;
        this.puerto = puerto;
        this.tiempoMs = tiempoMs;
        this.respuesta = respuesta;
    }

    public String toCsvRow() {
        return documento + "," + idCandidato + "," + ipMesa + "," + puerto + "," + tiempoMs + "," + respuesta;
    }
}
