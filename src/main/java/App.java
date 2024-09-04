import com.google.gson.JsonObject;
import db.JsonParser;
import service.SearchService;
import service.StatService;
import java.io.FileWriter;
import java.io.IOException;


public class App {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java -jar program.jar <operation> <inputFile> <outputFile>");
            return;
        }

        String operation = args[0];
        String inputFile = args[1];
        String outputFile = args[2];


        try {
            JsonObject inputJson = JsonParser.parseJson(inputFile);
            if ("search".equals(operation)) {
                SearchService searchService = new SearchService();
                searchService.processSearch(inputJson, outputFile);
            } else if ("stat".equals(operation)) {
                StatService statService = new StatService();
                statService.processStat(inputJson, outputFile);
            } else {
                System.out.println("Unknown operation: " + operation);
            }
        } catch (Exception e) {
            logError(outputFile, e.getMessage());
        }
    }

    private static void logError(String outputFile, String errorMessage) {
        try (FileWriter writer = new FileWriter(outputFile, true)) {
            writer.write("Error: " + errorMessage + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
