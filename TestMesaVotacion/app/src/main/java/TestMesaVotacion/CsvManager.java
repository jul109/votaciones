package TestMesaVotacion;

import java.io.*;
import java.util.*;

public class CsvManager {

    private static final String DEFAULT_FILE_NAME = "votos.csv";
    private static final String DEFAULT_DELIMITER = ",";
    private static final String RESULT_FILE_NAME = "test_results.csv";

    private final Map<String, List<VotoTest>> votesByIpAndPortMap;

    public CsvManager() {
        this.votesByIpAndPortMap = new HashMap<>();

        try {
            loadVotesFromCsv(DEFAULT_FILE_NAME, DEFAULT_DELIMITER);
        } catch (IOException e) {
            System.err.println("❌ Error al cargar los votos desde el CSV: " + e.getMessage());
        }
    }

    private void loadVotesFromCsv(String filePath, String delimiter) throws IOException {
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
                if (parts.length != 4) {
                    System.err.println("⚠️ Línea con formato incorrecto (" + parts.length + " partes): " + line);
                    continue;
                }

                try {
                    String documento = parts[0].trim();
                    int idCandidato = Integer.parseInt(parts[1].trim());
                    String ipMesa = parts[2].trim();
                    String puerto = parts[3].trim();

                    VotoTest voto = new VotoTest(documento, idCandidato, ipMesa, puerto);
                    System.out.println("✔️ Voto cargado: " + voto);

                    String key = ipMesa + ":" + puerto;
                    votesByIpAndPortMap.computeIfAbsent(key, k -> new ArrayList<>()).add(voto);

                } catch (NumberFormatException e) {
                    System.err.println("⚠️ Error al convertir idCandidato a número: " + Arrays.toString(parts));
                }
            }
        }
    }

    public Map<String, List<VotoTest>> getVotesByIpAndPortMap() {
        return Collections.unmodifiableMap(votesByIpAndPortMap);
    }

    public void guardarResultados(List<ResultadoVoto> resultados) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESULT_FILE_NAME))) {
            writer.write("documento,idCandidato,ipMesa,puerto,tiempoMs,respuesta\n");
            for (ResultadoVoto r : resultados) {
                writer.write(r.toCsvRow());
                writer.write("\n");
            }
            System.out.println("📄 Resultados guardados en " + RESULT_FILE_NAME);
        } catch (IOException e) {
            System.err.println("❌ Error al escribir archivo de resultados: " + e.getMessage());
        }
    }
}

