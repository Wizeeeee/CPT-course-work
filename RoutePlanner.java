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

        List<List<String>> permutations = generatePermutations(intermediates);
        CityGraph.PathResult bestResult = null;

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
            }

            CityGraph.PathResult finalRes = cityGraph.shortestPath(current, end);
            if (finalRes == null) continue;

            fullPath.addAll(finalRes.getPath());
            totalDist += finalRes.getTotalDistance();

            if (bestResult == null || totalDist < bestResult.getTotalDistance()) {
                bestResult = new CityGraph.PathResult(fullPath, totalDist);
            }
        }

        return bestResult;
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
