package MesaVotacion;

public class Votante {
    private String id;

    public Votante(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del votante no puede ser nulo o vacío.");
        }
        this.id = id;
    }

    public String getId() {
        return id;
    }
    public String toCsvString() {
        return id;
    }

    public static Votante fromCsvString(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            
            throw new IllegalArgumentException("La línea CSV para Votante no puede ser nula o vacía.");
        }
        return new Votante(csvLine.trim());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Votante votante = (Votante) o;
        return id.equals(votante.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Votante{id='" + id + "'}";
    }
}