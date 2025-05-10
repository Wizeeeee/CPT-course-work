import java.util.*;

public class CityGraph {
    private Map<String, List<Edge>> adjacencyList;

    public CityGraph() {
        adjacencyList = new HashMap<>();
    }

    public boolean hasCity(String city) {
        return adjacencyList.containsKey(city);
    }

    public void addEdge(String cityA, String cityB, double distance) {
        adjacencyList.computeIfAbsent(cityA, k -> new ArrayList<>()).add(new Edge(cityB, distance));
        adjacencyList.computeIfAbsent(cityB, k -> new ArrayList<>()).add(new Edge(cityA, distance));
    }

    public PathResult shortestPath(String start, String end) {
        if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(end)) 
            return null;

        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();

        // 关键修复1: 显式初始化起点的前驱为null
        predecessors.put(start, null);

        for (String city : adjacencyList.keySet()) 
            distances.put(city, Double.POSITIVE_INFINITY);
        distances.put(start, 0.0);
        queue.add(new Node(start, 0.0));

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.city.equals(end)) break;
            if (current.distance > distances.get(current.city)) continue;

            for (Edge edge : adjacencyList.getOrDefault(current.city, Collections.emptyList())) {
                double newDist = current.distance + edge.distance;
                if (newDist < distances.getOrDefault(edge.targetCity, Double.POSITIVE_INFINITY)) {
                    distances.put(edge.targetCity, newDist);
                    predecessors.put(edge.targetCity, current.city); // 记录前驱节点
                    queue.add(new Node(edge.targetCity, newDist));
                }
            }
        }

        if (distances.get(end) == Double.POSITIVE_INFINITY)
            return null;

        // 关键修复2: 重构路径构建逻辑
        LinkedList<String> path = new LinkedList<>();
        String currentCity = end;
        while (currentCity != null) {
            path.addFirst(currentCity);
            currentCity = predecessors.get(currentCity);
        }

        // 确保路径包含起点（处理直接连接的情况）
        if (!path.getFirst().equals(start)) 
            path.addFirst(start);

        return new PathResult(path, distances.get(end));
    }

    // -------------------- 内部类 --------------------
    private static class Edge {
        String targetCity;
        double distance;
        Edge(String targetCity, double distance) {
            this.targetCity = targetCity;
            this.distance = distance;
        }
    }

    private static class Node {
        String city;
        double distance;
        Node(String city, double distance) {
            this.city = city;
            this.distance = distance;
        }
    }

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