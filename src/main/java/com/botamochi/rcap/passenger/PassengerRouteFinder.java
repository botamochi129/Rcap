package com.botamochi.rcap.passenger;

import mtr.data.Platform;
import mtr.data.RailwayData;
import mtr.data.Route;
import mtr.data.DataCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PassengerRouteFinder {

    /**
     * 住宅とオフィスのBlockPos(long)から最寄りプラットフォームIDを検索し、
     * 同一ルートならその区間のプラットフォームIDリストを返す。
     * それ以外は徒歩直行想定の簡単なリストを返す。
     *
     * @param world MinecraftのLevel
     * @param homeBlockPos 住宅のBlockPos.long値
     * @param officeBlockPos オフィスのBlockPos.long値
     * @return プラットフォームIDのリスト（ルート）
     */
    public static List<Long> findRoute(World world, long homeBlockPos, long officeBlockPos) {
        RailwayData railwayData = RailwayData.getInstance(world);
        DataCache dataCache = railwayData.dataCache;

        BlockPos homePos = BlockPos.fromLong(homeBlockPos);
        BlockPos officePos = BlockPos.fromLong(officeBlockPos);

        long homePlatformId = RailwayData.getClosePlatformId(railwayData.platforms, dataCache, homePos);
        long officePlatformId = RailwayData.getClosePlatformId(railwayData.platforms, dataCache, officePos);

        if (homePlatformId == 0 || officePlatformId == 0) {
            return new ArrayList<>(); // 徒歩やなし
        }

        if (homePlatformId == officePlatformId) {
            List<Long> single = new ArrayList<>();
            single.add(homePlatformId);
            return single;
        }

        Optional<Route> homeRoute = railwayData.routes.stream()
                .filter(route -> route.platformIds.stream().anyMatch(p -> p.platformId == homePlatformId))
                .findFirst();

        Optional<Route> officeRoute = railwayData.routes.stream()
                .filter(route -> route.platformIds.stream().anyMatch(p -> p.platformId == officePlatformId))
                .findFirst();

        if (homeRoute.isPresent() && officeRoute.isPresent() && homeRoute.get() == officeRoute.get()) {
            var routePlatforms = homeRoute.get().platformIds;
            List<Long> platformIdList = new ArrayList<>();
            for (var p : routePlatforms) platformIdList.add(p.platformId);

            int homeIndex = platformIdList.indexOf(homePlatformId);
            int officeIndex = platformIdList.indexOf(officePlatformId);

            if (homeIndex > officeIndex) {
                List<Long> subList = new ArrayList<>(platformIdList.subList(officeIndex, homeIndex + 1));
                java.util.Collections.reverse(subList);
                return subList;
            } else if (homeIndex >= 0 && officeIndex >= 0) {
                return new ArrayList<>(platformIdList.subList(homeIndex, officeIndex + 1));
            } else {
                return platformIdList;
            }
        }

        // TODO: 複数路線ルート探索（乗換え）実装可能
        ArrayList<Long> direct = new ArrayList<>();
        direct.add(homePlatformId);
        direct.add(officePlatformId);
        return direct;
    }
}
