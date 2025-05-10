import java.util.*;

public class RoutePlanner {
    private final CityGraph cityGraph;
    private final AttractionManager attractionManager;

    public RoutePlanner(CityGraph cityGraph, AttractionManager attractionManager) {
        this.cityGraph = cityGraph;
        this.attractionManager = attractionManager;
    }

    public CityGraph.PathResult planRoute(String start, String end, List<String> attractions) {
        // 1. 获取所有关键点（起点、终点、景点对应城市）
        List<String> keyCities = new ArrayList<>();
        keyCities.add(start);
        keyCities.add(end);
        for (String attraction : attractions) {
            String city = attractionManager.getCity(attraction);
            if (city != null && !keyCities.contains(city)) {
                keyCities.add(city);
            }
        }
        int n = keyCities.size() - 2; // 中间景点数量

        // 无中间景点时直接返回最短路径
        if (n == 0) {
            return cityGraph.shortestPath(start, end);
        }

        // 2. 预计算关键点之间的最短距离
        double[][] dist = precomputeDistances(keyCities);

        // 3. 动态规划求解
        int maskSize = 1 << n;
        double[][] dp = new double[maskSize][n + 2];  // dp[mask][u]
        int[][] predecessor = new int[maskSize][n + 2]; // 记录前驱节点

        // 初始化：所有状态初始为无穷大
        for (double[] row : dp) Arrays.fill(row, Double.POSITIVE_INFINITY);
        dp[0][0] = 0; // 起点状态初始化

        // 状态转移
        for (int mask = 0; mask < maskSize; mask++) {
            for (int u = 0; u < n + 2; u++) {
                if (dp[mask][u] == Double.POSITIVE_INFINITY) continue;

                // 访问未访问的中间景点
                for (int v = 1; v <= n; v++) {
                    if ((mask & (1 << (v - 1))) == 0) { // 检查是否未访问
                        int newMask = mask | (1 << (v - 1));
                        double newDist = dp[mask][u] + dist[u][v];
                        if (newDist < dp[newMask][v]) {
                            dp[newMask][v] = newDist;
                            predecessor[newMask][v] = u; // 记录前驱节点
                        }
                    }
                }

                // 直达终点
                double finalDist = dp[mask][u] + dist[u][n + 1];
                if (finalDist < dp[mask][n + 1]) {
                    dp[mask][n + 1] = finalDist;
                    predecessor[mask][n + 1] = u;
                }
            }
        }

        // 4. 提取结果
        double minDist = dp[maskSize - 1][n + 1];
        if (minDist == Double.POSITIVE_INFINITY) return null;

        // 5. 重建路径
        List<String> path = reconstructPath(keyCities, predecessor, n);
        return new CityGraph.PathResult(path, minDist);
    }

    // 预计算关键点间的最短距离
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

    // 路径重建
    private List<String> reconstructPath(List<String> keyCities, int[][] predecessor, int n) {
        List<String> path = new ArrayList<>();
        int mask = (1 << n) - 1; // 所有中间景点已访问
        int current = n + 1;      // 终点索引

        // 反向追踪路径
        while (current != 0) { // 起点索引为0
            path.add(0, keyCities.get(current));
            int prev = predecessor[mask][current];
            if (prev >= 1 && prev <= n) { // 中间景点需更新掩码
                mask &= ~(1 << (prev - 1));
            }
            current = prev;
        }
        path.add(0, keyCities.get(0)); // 添加起点
        return path;
    }
}