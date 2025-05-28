package MesaVotacion;

import java.util.Objects;
import java.util.UUID; // Importar UUID

public class VotoPendiente {
    private String id; // Nuevo campo para el ID único del voto pendiente
    private int mesaId;
    private int candidatoId;

    
    public VotoPendiente(int mesaId, int candidatoId) {
        this.id = UUID.randomUUID().toString(); // Generar ID único
        this.mesaId = mesaId;
        this.candidatoId = candidatoId;
    }

    
    private VotoPendiente(String id, int mesaId, int candidatoId) {
        this.id = id;
        this.mesaId = mesaId;
        this.candidatoId = candidatoId;
    }

    public String getId() {
        return id;
    }

    public int getMesaId() {
        return mesaId;
    }

    public int getCandidatoId() {
        return candidatoId;
    }

    public String toCsvString() {
        return id + "," + mesaId + "," + candidatoId;
    }

    public static VotoPendiente fromCsvString(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            throw new IllegalArgumentException("La línea CSV no puede ser nula o vacía.");
        }
        String[] parts = csvLine.split(",", 3); // Dividir en 3 partes como máximo
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "La línea CSV no tiene el formato esperado (esperaba 3 partes: id,mesaId,candidatoId): " + csvLine);
        }
        try {
            String id = parts[0].trim();
            int mesaId = Integer.parseInt(parts[1].trim());
            int candidatoId = Integer.parseInt(parts[2].trim());
            // Usar el constructor privado que acepta un ID
            return new VotoPendiente(id, mesaId, candidatoId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error al parsear números desde la línea CSV: " + csvLine, e);
        }
    }

    @Override
    public String toString() {
        return "VotoPendiente{" +
                "id='" + id + '\'' + // Incluir ID en toString
                ", mesaId=" + mesaId +
                ", candidatoId=" + candidatoId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        VotoPendiente that = (VotoPendiente) o;
        return Objects.equals(id, that.id); 
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); 
    }
}