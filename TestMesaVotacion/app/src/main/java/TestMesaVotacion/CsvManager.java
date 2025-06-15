package TestMesaVotacion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CsvManager {

    private static final String DEFAULT_FILE_NAME = "votos.csv";
    private static final String DEFAULT_DELIMITER = ","; 

    private Map<String, VotoTest> votesMap;

    public CsvManager() {
        this.votesMap = new HashMap<>();
        try {
            loadVotesFromCsv(DEFAULT_FILE_NAME, DEFAULT_DELIMITER);
        } catch (IOException e) {
            System.err.println("Error al cargar los votos por defecto en CsvManager: " + e.getMessage());
        }
    }

    private void loadVotesFromCsv(String filePath, String delimiter) throws IOException {
        this.votesMap.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] parts = line.split(delimiter);

                
                if (parts.length == 4) { 
                    String documento = parts[0].trim();
                    
                    
                    int idCandidato = Integer.parseInt(parts[1].trim()); 
                    
                    String candidatoSeleccionadoNombre = parts[2].trim(); 
                    String ipMesa = parts[3].trim();

                    
                    VotoTest voto = new VotoTest(documento, idCandidato, candidatoSeleccionadoNombre, ipMesa);
                    this.votesMap.put(documento, voto);
                } else {
                    System.err.println("Advertencia: Línea con formato incorrecto omitida: " + line);
                }
            }
        }
    }

    public Map<String, VotoTest> getVotesMap() {
        return votesMap;
    }
}