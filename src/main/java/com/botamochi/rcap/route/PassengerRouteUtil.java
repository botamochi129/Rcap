package com.botamochi.rcap.route;

import mtr.client.ClientData;
import mtr.data.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/**
 * MTR本家の経路探索ロジックを参考に、
 * [入口, 乗車プラットフォーム, 乗る電車ID, ..., 乗換プラットフォーム, 乗る電車ID, ..., 降車プラットフォーム, 出口]
 * のルート配列を構築するユーティリティ
 */
public class PassengerRouteUtil {

    /**
     * 入口から出口までの全ルート情報を構築
     */
    public static List<Object> findFullRoute(ServerWorld world, BlockPos entrance, BlockPos exit) {
        RailwayData railwayData = RailwayData.getInstance(world);

        // 入口・出口の駅を特定
        Station fromStation = RailwayData.getStation(railwayData.stations, railwayData.dataCache, entrance);
        Station toStation = RailwayData.getStation(railwayData.stations, railwayData.dataCache, exit);
        System.out.println("fromStation=" + fromStation + ", toStation=" + toStation);
        if (fromStation == null || toStation == null) return Collections.emptyList();

        // 最寄り乗車プラットフォーム＆降車プラットフォームの検索
        Platform startPlatform = getNearestPlatform(railwayData, fromStation.id, entrance);
        Platform endPlatform = getNearestPlatform(railwayData, toStation.id, exit);        System.out.println("startPlatform=" + startPlatform + ", endPlatform=" + endPlatform);
        if (startPlatform == null || endPlatform == null) return Collections.emptyList();

        // 経路探索本体（MTR PathFinderの簡易再現）
        List<RouteStep> routeSteps = findRouteSteps(railwayData, startPlatform, endPlatform);

        // 配列組み立て
        List<Object> route = new ArrayList<>();
        route.add(entrance);
        for (RouteStep step : routeSteps) {
            route.add(step.platform.getMidPos());
            route.add(step.trainId);
        }
        route.add(exit);

        return route;
    }

    /**
     * MTR本家のPathFinder#findRoutePlatformsを簡易再現
     * ルート上のPlatformとTrainIdをステップごとに返す
     */
    public static List<RouteStep> findRouteSteps(RailwayData railwayData, Platform start, Platform end) {
        // Dijkstra/BFSで各platformへの最短経路と乗換情報を探索
        // 本家のPathFinder#findRoutePlatformsを参考にする
        DataCache dataCache = railwayData.dataCache;
        Map<Long, Platform> platformIdMap = dataCache.platformIdMap;

        // BFS用ノード: platformId, 経路, 直前のtrainId, 乗換回数
        class Node {
            long platformId;
            List<RouteStep> path;
            long trainId;
            int transfers;
            Node(long platformId, List<RouteStep> path, long trainId, int transfers) {
                this.platformId = platformId;
                this.path = path;
                this.trainId = trainId;
                this.transfers = transfers;
            }
        }

        Queue<Node> queue = new LinkedList<>();
        Set<Long> visited = new HashSet<>();
        queue.add(new Node(start.id, new ArrayList<>(), -1, 0));

        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (!visited.add(node.platformId)) continue;
            Platform platform = platformIdMap.get(node.platformId);

            // ゴール判定
            if (platform.id == end.id) {
                return node.path;
            }

            // このplatformに停車する全てのtrainIdを調べる
            for (Route route : railwayData.routes) {
                for (Route.RoutePlatform rp : route.platformIds) {
                    if (rp.platformId == platform.id) {
                        long trainId = route.id; // 本家はTrainId, ここではRouteIdを代理
                        // 同じtrainなら乗換不要
                        List<RouteStep> newPath = new ArrayList<>(node.path);
                        if (trainId != node.trainId) {
                            // 乗換/乗車する場合のみステップ追加
                            newPath.add(new RouteStep(platform, trainId));
                        }
                        // 隣接platformへ進む
                        int idx = route.platformIds.indexOf(rp);
                        // 前方
                        if (idx + 1 < route.platformIds.size()) {
                            long nextPlatformId = route.platformIds.get(idx + 1).platformId;
                            queue.add(new Node(nextPlatformId, newPath, trainId, trainId != node.trainId ? node.transfers + 1 : node.transfers));
                        }
                        // 後方
                        if (idx - 1 >= 0) {
                            long prevPlatformId = route.platformIds.get(idx - 1).platformId;
                            queue.add(new Node(prevPlatformId, newPath, trainId, trainId != node.trainId ? node.transfers + 1 : node.transfers));
                        }
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    public static Platform getNearestPlatform(RailwayData data, long stationId, BlockPos from) {
        // サーバー側用
        double minDist = Double.MAX_VALUE;
        Platform nearest = null;
        for (Platform p : getPlatformsForStation(data, stationId)) {
            double d = p.getMidPos().getSquaredDistance(from);
            if (d < minDist) {
                minDist = d;
                nearest = p;
            }
        }
        return nearest;
    }

    public static List<Platform> getPlatformsForStation(RailwayData data, long stationId) {
        List<Platform> platforms = new ArrayList<>();
        for (Platform p : data.dataCache.platformIdMap.values()) {
            if (data.dataCache.platformIdToStation.get(p.id).id == stationId) {
                platforms.add(p);
            }
        }
        return platforms;
    }

    public static class RouteStep {
        public final Platform platform;
        public final long trainId;
        public RouteStep(Platform platform, long trainId) {
            this.platform = platform;
            this.trainId = trainId;
        }
    }
}