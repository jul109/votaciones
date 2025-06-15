package TestMesaVotacion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvManager {

    private static final String DEFAULT_FILE_NAME = "votos.csv";
    private static final String DEFAULT_DELIMITER = ","; 

    private Map<String, VotoTest> votesMap;
    private Map<String, List<VotoTest>> votesByIpAndPortMap; 

    public CsvManager() {
        this.votesMap = new HashMap<>();
        this.votesByIpAndPortMap = new HashMap<>();

        try {
            loadVotesFromCsv(DEFAULT_FILE_NAME, DEFAULT_DELIMITER);
        } catch (IOException e) {
            System.err.println("Error al cargar los votos por defecto en CsvManager: " + e.getMessage());
        }
    }

    private void loadVotesFromCsv(String filePath, String delimiter) throws IOException {
        this.votesMap.clear();
        this.votesByIpAndPortMap.clear();

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
                    String ipMesa = parts[2].trim(); 
                    String puerto = parts[3].trim(); 

                    VotoTest voto = new VotoTest(documento, idCandidato, ipMesa, puerto);
                    this.votesMap.put(documento, voto);

                    String ipPortKey = ipMesa + ":" + puerto;
                    this.votesByIpAndPortMap.computeIfAbsent(ipPortKey, k -> new ArrayList<>()).add(voto);

                } else {
                    System.err.println("Advertencia: Línea con formato incorrecto (" + parts.length + " partes) omitida: " + line);
                }
            }
        }
    }

    public Map<String, VotoTest> getVotesMap() {
        return votesMap;
    }

    public Map<String, List<VotoTest>> getVotesByIpAndPortMap() {
        return votesByIpAndPortMap;
    }
}