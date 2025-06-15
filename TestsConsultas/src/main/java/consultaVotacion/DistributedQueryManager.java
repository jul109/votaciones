package consultaVotacion;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedQueryManager {
    private final int NUM_DEVICES;
    private final ExecutorService executorService;
    private final List<String> documents;
    private final String outputFile;
    private final AtomicInteger completedQueries = new AtomicInteger(0);
    private final int totalDocuments;

    public DistributedQueryManager(String inputFile, String outputFile, String numDevices) throws IOException {
        this.documents = readDocuments(inputFile);
        this.outputFile = outputFile;
        this.totalDocuments = documents.size();
        this.NUM_DEVICES = Integer.parseInt(numDevices);
        this.executorService = Executors.newFixedThreadPool(NUM_DEVICES);
        // Write CSV headers
        writeHeaders();
    }

    private void writeHeaders() {
        try (FileWriter fw = new FileWriter(outputFile);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("Dispositivo,Documento,Tiempo_Respuesta(ms),Respuesta");
        } catch (IOException e) {
            System.err.println("Error writing headers: " + e.getMessage());
        }
    }

    private List<String> readDocuments(String inputFile) throws IOException {
        List<String> docs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                docs.add(line.trim());
            }
        }
        return docs;
    }

    public void processDocuments() {
        int documentsPerDevice = (int) Math.ceil((double) totalDocuments / NUM_DEVICES);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < NUM_DEVICES; i++) {
            final int deviceNumber = i + 1;
            final int startIndex = i * documentsPerDevice;
            final int endIndex = Math.min(startIndex + documentsPerDevice, totalDocuments);

            futures.add(executorService.submit(() -> {
                try {
                    processDeviceDocuments(deviceNumber, startIndex, endIndex);
                } catch (Exception e) {
                    System.err.println("Error processing device " + deviceNumber + ": " + e.getMessage());
                }
            }));
        }

        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                System.err.println("Error waiting for task completion: " + e.getMessage());
            }
        }

        executorService.shutdown();
    }

    private void processDeviceDocuments(int deviceNumber, int startIndex, int endIndex) {
        System.out.println("Device " + deviceNumber + " processing documents from " + startIndex + " to " + endIndex);

        CitizenController controller = new CitizenController();
        for (int i = startIndex; i < endIndex; i++) {
            String document = documents.get(i);
            long startTime = System.currentTimeMillis();
            
            try {
                
                String response = controller.queryDocument(document);
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                // Write result to CSV
                writeResult(deviceNumber, document, duration, response);
                
                // Update progress
                int completed = completedQueries.incrementAndGet();
                System.out.printf("Progress: %d/%d documents processed (%.2f%%)\n", 
                    completed, totalDocuments, (completed * 100.0 / totalDocuments));
            } catch (Exception e) {
                System.err.println("Error processing document " + document + " on device " + deviceNumber + ": " + e.getMessage());
            }
        }
        controller.shutdown();
    }

    private synchronized void writeResult(int deviceNumber, String document, long duration, String response) {
        try (FileWriter fw = new FileWriter(outputFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.printf("%d,%s,%d,%s%n", deviceNumber, document, duration, response);
        } catch (IOException e) {
            System.err.println("Error writing result: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java DistributedQueryManager <input_file> <output_file> <num_devices>");
            System.exit(1);
        }

        try {
            DistributedQueryManager manager = new DistributedQueryManager(args[0], args[1], args[2]);
            manager.processDocuments();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 