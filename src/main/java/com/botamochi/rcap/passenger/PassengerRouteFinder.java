package com.botamochi.rcap.passenger;

import org.mtr.core.data.*;

import java.util.*;

/**
 * PassengerRouteFinder:
 * MTR4のウェブルート検索の設計を参考に、より堅牢なプラットフォーム間経路探索を実装
 */
public class PassengerRouteFinder {

    /**
     * fromPlatformId から toPlatformId へのプラットフォームID のリストを返す
     */
    public static List<Long> findRoute(Data data, long fromPlatformId, long toPlatformId) {
        Map<Long, List<Long>> graph = buildGraph(data);

        // デバッグ
        System.out.println("routes: " + data.routes.size());
        System.out.println("platforms: " + data.platforms.size());
        System.out.println("stations: " + data.stations.size());
        System.out.println("Graph: " + graph);

        // BFSで経路探索
        Queue<Long> queue = new LinkedList<>();
        Map<Long, Long> previous = new HashMap<>();
        Set<Long> visited = new HashSet<>();

        queue.add(fromPlatformId);
        visited.add(fromPlatformId);

        while (!queue.isEmpty()) {
            long current = queue.poll();
            if (current == toPlatformId) break;

            for (long neighbor : graph.getOrDefault(current, Collections.emptyList())) {
                if (visited.add(neighbor)) { // 既訪問でなければ
                    queue.add(neighbor);
                    previous.put(neighbor, current);
                }
            }
        }

        // 経路復元
        List<Long> path = new LinkedList<>();
        for (Long at = toPlatformId; at != null; at = previous.get(at)) {
            path.add(0, at);
        }

        if (!path.isEmpty() && path.get(0).equals(fromPlatformId)) {
            System.out.println("Route found from " + fromPlatformId + " to " + toPlatformId + ": " + path);
            return path;
        } else {
            System.out.println("Route not found from " + fromPlatformId + " to " + toPlatformId);
            return Collections.emptyList();
        }
    }

    /**
     * Data.routes, Data.stations, Data.platforms から
     * プラットフォームID同士の接続グラフを構築
     */
    private static Map<Long, List<Long>> buildGraph(Data data) {
        Map<Long, List<Long>> graph = new HashMap<>();

        // 1. 路線順序でプラットフォームを接続
        for (Route route : data.routes) {
            List<RoutePlatformData> routePlatforms = route.getRoutePlatforms();
            for (int i = 0; i < routePlatforms.size() - 1; i++) {
                long a = routePlatforms.get(i).platform.getId();
                long b = routePlatforms.get(i + 1).platform.getId();
                graph.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
                graph.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
            }
        }

        // 2. 駅ごとにプラットフォームを集約し、同一駅内で全て双方向接続
        Map<Long, List<Long>> stationToPlatforms = new HashMap<>();
        for (Platform platform : data.platforms) {
            if (platform.area instanceof Station) {
                long stationId = ((Station) platform.area).getId();
                stationToPlatforms.computeIfAbsent(stationId, k -> new ArrayList<>()).add(platform.getId());
            }
        }
        for (List<Long> platformIds : stationToPlatforms.values()) {
            for (int i = 0; i < platformIds.size(); i++) {
                for (int j = i + 1; j < platformIds.size(); j++) {
                    long a = platformIds.get(i);
                    long b = platformIds.get(j);
                    graph.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
                    graph.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
                }
            }
        }

        // 3. 無効なプラットフォームIDを除外（孤立ノード対策）
        Set<Long> validPlatformIds = new HashSet<>();
        for (Platform p : data.platforms) validPlatformIds.add(p.getId());
        graph.keySet().removeIf(id -> !validPlatformIds.contains(id));

        return graph;
    }
}
