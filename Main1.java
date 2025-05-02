import java.io.*;
import java.util.*;

public class Main1 {
    public static void main(String[] args) {
        // 1. 加载景点数据
        AttractionManager attractionManager = new AttractionManager();
        try {
            attractionManager.loadAttractions("attractions.csv");
        } catch (IOException e) {
            System.err.println("无法加载景点文件: " + e.getMessage());
            return;
        }

        // 2. 构建道路图
        CityGraph cityGraph = new CityGraph();
        try (BufferedReader br = new BufferedReader(new FileReader("roads.csv"))) {
            br.readLine(); // 跳过表头
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) continue; // 忽略格式错误的行
                String cityA = parts[0].trim();
                String cityB = parts[1].trim();
                double distance = Double.parseDouble(parts[2].trim());
                cityGraph.addEdge(cityA, cityB, distance); // 添加双向边
            }
        } catch (IOException e) {
            System.err.println("无法加载道路文件: " + e.getMessage());
            return;
        } catch (NumberFormatException e) {
            System.err.println("道路文件格式错误: 距离应为数字");
            return;
        }

        // 3. 处理用户输入
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入起点城市（格式：城市 州缩写，如 New York NY）: ");
        String startCity = scanner.nextLine().trim();
        System.out.print("请输入终点城市（格式：城市 州缩写，如 Miami FL）: ");
        String endCity = scanner.nextLine().trim();

        // 验证输入城市是否存在
        if (!cityGraph.hasCity(startCity)) {
            System.err.println("错误: 起点城市不存在于道路网络中");
            return;
        }
        if (!cityGraph.hasCity(endCity)) {
            System.err.println("错误: 终点城市不存在于道路网络中");
            return;
        }

        System.out.print("请输入景点列表（逗号分隔，如 Hollywood Sign, Liberty Bell）: ");
        List<String> attractions = Arrays.asList(scanner.nextLine().trim().split("\\s*,\\s*"));

        // 4. 路径规划
        RoutePlanner routePlanner = new RoutePlanner(cityGraph, attractionManager);
        CityGraph.PathResult result = routePlanner.planRoute(startCity, endCity, attractions);

        // 5. 输出结果
        if (result == null) {
            System.out.println("未找到有效路径！");
        } else {
            System.out.println("\n===== 最优路径 =====");
            System.out.println("起点: " + startCity);
            System.out.println("终点: " + endCity);
            System.out.println("景点: " + attractions);
            System.out.println("路径: " + result.getPath());
            System.out.printf("总距离: %.1f 英里\n", result.getTotalDistance());
        }
    }
}