import java.util.*;

public class CityGraph {
    private Map<String, List<Edge>> adjacencyList; // Adjacency list to represent the graph

    public CityGraph() {
        adjacencyList = new HashMap<>();
    }

    // Checks if a city exists in the graph
    public boolean hasCity(String city) {
        return adjacencyList.containsKey(city);
    }

    // Adds an edge between two cities with a specified distance
    public void addEdge(String cityA, String cityB, double distance) {
        adjacencyList.computeIfAbsent(cityA, k -> new ArrayList<>()).add(new Edge(cityB, distance));
        adjacencyList.computeIfAbsent(cityB, k -> new ArrayList<>()).add(new Edge(cityA, distance));
    }

    // Finds the shortest path between two cities using Dijkstra's algorithm
    public PathResult shortestPath(String start, String end) {
        // Return null if either start or end city does not exist in the graph
        if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(end)) 
            return null;

        // Priority queue to process nodes based on their distance
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>(); 

        // Initialize the start city
        predecessors.put(start, null);
        for (String city : adjacencyList.keySet()) 
            distances.put(city, Double.POSITIVE_INFINITY); 
        distances.put(start, 0.0); 
        queue.add(new Node(start, 0.0)); 

        // Process the priority queue
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.city.equals(end)) break; 
            if (current.distance > distances.get(current.city)) continue; 

            // Iterate through all neighbors of the current city
            for (Edge edge : adjacencyList.getOrDefault(current.city, Collections.emptyList())) {
                double newDist = current.distance + edge.distance;
                if (newDist < distances.getOrDefault(edge.targetCity, Double.POSITIVE_INFINITY)) {
                    distances.put(edge.targetCity, newDist); // Update the shortest distance
                    predecessors.put(edge.targetCity, current.city); // Update the predecessor
                    queue.add(new Node(edge.targetCity, newDist));
                }
            }
        }

        // If the end city is unreachable, return null
        if (distances.get(end) == Double.POSITIVE_INFINITY)
            return null;

        // Reconstruct the shortest path from the predecessors map
        LinkedList<String> path = new LinkedList<>();
        String currentCity = end;
        while (currentCity != null) {
            path.addFirst(currentCity); // Add the city to the path
            currentCity = predecessors.get(currentCity); // Move to the predecessor
        }

        // Ensure the path starts with the start city
        if (!path.getFirst().equals(start)) 
            path.addFirst(start);

        // Return the result containing the path and total distance
        return new PathResult(path, distances.get(end));
    }

    // -------------------- Inner Classes --------------------
    // Represents an edge in the graph
    private static class Edge {
        String targetCity; 
        double distance; 
        Edge(String targetCity, double distance) {
            this.targetCity = targetCity;
            this.distance = distance;
        }
    }

    // Represents a node in the priority queue
    private static class Node {
        String city; 
        double distance; 
        Node(String city, double distance) {
            this.city = city;
            this.distance = distance;
        }
    }

    // Represents the result of the shortest path calculation
    public static class PathResult {
        private final List<String> path;
        private final double totalDistance; 
        public PathResult(List<String> path, double totalDistance) {
            this.path = path;
            this.totalDistance = totalDistance;
        }
        public List<String> getPath() { return path; }
        public double getTotalDistance() { return totalDistance; }
    }
}