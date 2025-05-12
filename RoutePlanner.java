import java.util.*;

public class RoutePlanner {
    private final CityGraph cityGraph;
    private final AttractionManager attractionManager;

    public RoutePlanner(CityGraph cityGraph, AttractionManager attractionManager) {
        this.cityGraph = cityGraph;
        this.attractionManager = attractionManager;
    }

    public CityGraph.PathResult planRoute(String start, String end, List<String> attractions) {
        // 1. Get all key points (start, end, and cities corresponding to attractions)
        List<String> keyCities = new ArrayList<>();
        keyCities.add(start);
        keyCities.add(end);
        for (String attraction : attractions) {
            String city = attractionManager.getCity(attraction);
            if (city != null && !keyCities.contains(city)) {
                keyCities.add(city);
            }
        }
        int n = keyCities.size() - 2; // Number of intermediate attractions

        // If there are no intermediate attractions, return the shortest path directly
        if (n == 0) {
            return cityGraph.shortestPath(start, end);
        }

        // 2. Precompute the shortest distances between key points
        double[][] dist = precomputeDistances(keyCities);

        // 3. Solve using dynamic programming
        int maskSize = 1 << n;
        double[][] dp = new double[maskSize][n + 2];  // dp[mask][u]
        int[][] predecessor = new int[maskSize][n + 2]; // Record predecessor nodes

        // Initialize: Set all states to infinity
        for (double[] row : dp) Arrays.fill(row, Double.POSITIVE_INFINITY);
        dp[0][0] = 0; // Initialize the starting state

        // State transition
        for (int mask = 0; mask < maskSize; mask++) {
            for (int u = 0; u < n + 2; u++) {
                if (dp[mask][u] == Double.POSITIVE_INFINITY) continue;

                // Visit unvisited intermediate attractions
                for (int v = 1; v <= n; v++) {
                    if ((mask & (1 << (v - 1))) == 0) { // Check if not visited
                        int newMask = mask | (1 << (v - 1));
                        double newDist = dp[mask][u] + dist[u][v];
                        if (newDist < dp[newMask][v]) {
                            dp[newMask][v] = newDist;
                            predecessor[newMask][v] = u; // Record predecessor node
                        }
                    }
                }

                // Directly to the end point
                double finalDist = dp[mask][u] + dist[u][n + 1];
                if (finalDist < dp[mask][n + 1]) {
                    dp[mask][n + 1] = finalDist;
                    predecessor[mask][n + 1] = u;
                }
            }
        }

        // 4. Extract the result
        double minDist = dp[maskSize - 1][n + 1];
        if (minDist == Double.POSITIVE_INFINITY) return null;

        // 5. Reconstruct the path
        List<String> path = reconstructPath(keyCities, predecessor, n);
        return new CityGraph.PathResult(path, minDist);
    }

    // Precompute the shortest distances between key points
    private double[][] precomputeDistances(List<String> keyCities) {
        int size = keyCities.size();
        double[][] dist = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                CityGraph.PathResult res = cityGraph.shortestPath(keyCities.get(i), keyCities.get(j));
                dist[i][j] = (res != null) ? res.getTotalDistance() : Double.POSITIVE_INFINITY;
            }
        }
        return dist;
    }

    // Reconstruct the path
    private List<String> reconstructPath(List<String> keyCities, int[][] predecessor, int n) {
        List<String> path = new ArrayList<>();
        int mask = (1 << n) - 1; // All intermediate attractions visited
        int current = n + 1;      // Index of the end point

        // Trace the path backward
        while (current != 0) { // Start point index is 0
            path.add(0, keyCities.get(current));
            int prev = predecessor[mask][current];
            if (prev >= 1 && prev <= n) { // Update the mask for intermediate attractions
                mask &= ~(1 << (prev - 1));
            }
            current = prev;
        }
        path.add(0, keyCities.get(0)); // Add the start point
        return path;
    }
}