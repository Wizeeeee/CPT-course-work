import java.io.*;
import java.util.*;

public class AttractionManager {
    private final Map<String, String> attractionMap = new HashMap<>();

    public void loadAttractions(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) attractionMap.put(parts[0].trim(), parts[1].trim());
            }
        }
    }

    public String getCity(String attraction) {
        return attractionMap.get(attraction);
    }
}
