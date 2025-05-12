import java.io.*;
import java.util.*;

public class Test {
    public static void main(String[] args) {
        // 1. Load attraction data
        AttractionManager attractionManager = new AttractionManager();
        try {
            attractionManager.loadAttractions("attractions.csv");
        } catch (IOException e) {
            System.err.println("Failed to load attractions file: " + e.getMessage());
            return;
        }

        // 2. Build the road graph
        CityGraph cityGraph = new CityGraph();
        try (BufferedReader br = new BufferedReader(new FileReader("roads.csv"))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) continue; // Skip malformed lines
                String cityA = parts[0].trim();
                String cityB = parts[1].trim();
                double distance = Double.parseDouble(parts[2].trim());
                cityGraph.addEdge(cityA, cityB, distance); // Add bidirectional edges
            }
        } catch (IOException e) {
            System.err.println("Failed to load roads file: " + e.getMessage());
            return;
        } catch (NumberFormatException e) {
            System.err.println("Invalid roads file format: distance must be numeric");
            return;
        }

        // 3. Handle user input
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter starting city (format: City ST, e.g., New York NY): ");
        String startCity = scanner.nextLine().trim();
        System.out.print("Enter destination city (format: City ST, e.g., Miami FL): ");
        String endCity = scanner.nextLine().trim();

        // Validate input cities
        if (!cityGraph.hasCity(startCity)) {
            System.err.println("Error: Starting city does not exist in the road network");
            return;
        }
        if (!cityGraph.hasCity(endCity)) {
            System.err.println("Error: Destination city does not exist in the road network");
            return;
        }

        System.out.print("Enter attractions (comma-separated, e.g., Hollywood Sign, Liberty Bell): ");
        List<String> attractions = Arrays.asList(scanner.nextLine().trim().split("\\s*,\\s*"));

        // 4. Plan the route
        RoutePlanner routePlanner = new RoutePlanner(cityGraph, attractionManager);
        CityGraph.PathResult result = routePlanner.planRoute(startCity, endCity, attractions);

        // 5. Output results
        if (result == null) {
            System.out.println("No valid route found!");
        } else {
            System.out.println("\n===== Optimal Route =====");
            System.out.println("Start: " + startCity);
            System.out.println("Destination: " + endCity);
            System.out.println("Attractions: " + attractions);
            System.out.println("Path: " + result.getPath());
            System.out.printf("Total Distance: %.1f miles\n", result.getTotalDistance());
        }
    }
}