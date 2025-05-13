import java.util.*;

public class RoutePlanner {
    private final CityGraph cityGraph;
    private final AttractionManager attractionManager;

    public RoutePlanner(CityGraph cityGraph, AttractionManager attractionManager) {
        this.cityGraph = cityGraph;
        this.attractionManager = attractionManager;
    }

    public CityGraph.PathResult planRoute(String start, String end, List<String> attractions) {
        Set<String> cities = new LinkedHashSet<>();
        for (String a : attractions) {
            String city = attractionManager.getCity(a);
            if (city != null) cities.add(city);
        }
        List<String> intermediates = new ArrayList<>(cities);

        if (intermediates.isEmpty()) return cityGraph.shortestPath(start, end);

        // Step 1: 使用贪心算法生成初始路径
        CityGraph.PathResult greedyResult = planRouteGreedy(start, end, intermediates);
        double bestDistance = greedyResult != null ? greedyResult.getTotalDistance() : Double.MAX_VALUE;
        CityGraph.PathResult bestResult = greedyResult;

        // Step 2: 使用排列枚举 + 剪枝优化
        List<List<String>> permutations = generatePermutations(intermediates);
        for (List<String> perm : permutations) {
            List<String> fullPath = new ArrayList<>();
            double totalDist = 0;
            String current = start;

            for (String city : perm) {
                CityGraph.PathResult res = cityGraph.shortestPath(current, city);
                if (res == null) break;
                fullPath.addAll(res.getPath().subList(0, res.getPath().size() - 1));
                totalDist += res.getTotalDistance();
                current = city;

                // 剪枝：如果当前路径距离已经超过最优解，停止计算
                if (totalDist >= bestDistance) break;
            }

            CityGraph.PathResult finalRes = cityGraph.shortestPath(current, end);
            if (finalRes == null) continue;

            fullPath.addAll(finalRes.getPath());
            totalDist += finalRes.getTotalDistance();

            if (totalDist < bestDistance) {
                bestDistance = totalDist;
                bestResult = new CityGraph.PathResult(fullPath, totalDist);
            }
        }

        return bestResult;
    }

    // 贪心算法：生成一个近似最优路径
    private CityGraph.PathResult planRouteGreedy(String start, String end, List<String> intermediates) {
        List<String> fullPath = new ArrayList<>();
        double totalDist = 0;
        String current = start;
        Set<String> remaining = new HashSet<>(intermediates);

        while (!remaining.isEmpty()) {
            String nearestCity = null;
            double shortestDist = Double.MAX_VALUE;

            for (String city : remaining) {
                CityGraph.PathResult res = cityGraph.shortestPath(current, city);
                if (res != null && res.getTotalDistance() < shortestDist) {
                    nearestCity = city;
                    shortestDist = res.getTotalDistance();
                }
            }

            if (nearestCity == null) return null; // 无法找到路径
            CityGraph.PathResult res = cityGraph.shortestPath(current, nearestCity);
            fullPath.addAll(res.getPath().subList(0, res.getPath().size() - 1));
            totalDist += res.getTotalDistance();
            current = nearestCity;
            remaining.remove(nearestCity);
        }

        CityGraph.PathResult finalRes = cityGraph.shortestPath(current, end);
        if (finalRes == null) return null;

        fullPath.addAll(finalRes.getPath());
        totalDist += finalRes.getTotalDistance();

        return new CityGraph.PathResult(fullPath, totalDist);
    }

    private List<List<String>> generatePermutations(List<String> list) {
        List<List<String>> result = new ArrayList<>();
        backtrack(result, new ArrayList<>(), list, new boolean[list.size()]);
        return result;
    }

    private void backtrack(List<List<String>> result, List<String> temp, List<String> list, boolean[] used) {
        if (temp.size() == list.size()) {
            result.add(new ArrayList<>(temp));
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            if (!used[i]) {
                used[i] = true;
                temp.add(list.get(i));
                backtrack(result, temp, list, used);
                temp.remove(temp.size() - 1);
                used[i] = false;
            }
        }
    }
}
