package com.botamochi.rcap.block;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.block.entity.HousingBlockEntity;

import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.Platform;
import mtr.data.RailwayData;
import mtr.data.Route;
import mtr.data.Station;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HousingBlock extends BlockWithEntity {
    public HousingBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HousingBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof HousingBlockEntity housingBlockEntity)) {
                return ActionResult.SUCCESS;
            }

            player.openHandledScreen(housingBlockEntity);

            // 住宅に保存されているオフィスを取得
            com.botamochi.rcap.block.entity.OfficeBlockEntity office = null;
            Long linkedOfficePosLong = housingBlockEntity.getLinkedOfficePosLong();
            if (linkedOfficePosLong != null) {
                office = com.botamochi.rcap.data.OfficeManager.getAll().stream()
                        .filter(o -> o.getPos().asLong() == linkedOfficePosLong)
                        .findFirst()
                        .orElse(null);
            }

            // なければランダムに選択し、BlockEntityに保存
            if (office == null) {
                office = com.botamochi.rcap.data.OfficeManager.getRandomAvailableOffice();
                if (office == null) {
                    player.sendMessage(Text.literal("利用可能なオフィスが見つかりません。"), false);
                    return ActionResult.SUCCESS;
                }
                housingBlockEntity.setLinkedOfficePosLong(office.getPos().asLong());
            }

            if (!(world instanceof ServerWorld serverWorld)) {
                player.sendMessage(Text.literal("サーバーワールドでのみ動作します。"), false);
                return ActionResult.SUCCESS;
            }

            var railwayData = mtr.data.RailwayData.getInstance(serverWorld);
            if (railwayData == null || railwayData.railwayDataRouteFinderModule == null) {
                player.sendMessage(Text.literal("RailwayDataまたは経路検索モジュールが取得できません。"), false);
                return ActionResult.SUCCESS;
            }

            long newId = System.currentTimeMillis();
            String name = "テスト乗客";

            BlockPos homePos = pos;
            BlockPos officePos = office.getPos();

            System.out.println("[HousingBlock] findRoute開始 homePos=" + homePos + " officePos=" + officePos);
            player.sendMessage(Text.literal("[HousingBlock] findRoute開始"), false);

            int maxTickTime = 400;

            // 住宅とオフィスそれぞれ近くのプラットフォームIDを取得
            long homePlatformId = RailwayData.getClosePlatformId(railwayData.platforms, railwayData.dataCache, homePos, 1000, 1000, 1000);
            long officePlatformId = RailwayData.getClosePlatformId(railwayData.platforms, railwayData.dataCache, officePos, 1000, 1000, 1000);

            if (homePlatformId == 0L) {
                player.sendMessage(Text.literal("住宅近辺に有効なプラットフォームが見つかりません。"), false);
                return ActionResult.SUCCESS;
            }
            if (officePlatformId == 0L) {
                player.sendMessage(Text.literal("オフィス近辺に有効なプラットフォームが見つかりません。"), false);
                return ActionResult.SUCCESS;
            }

            Station homeStation = railwayData.dataCache.platformIdToStation.get(homePlatformId);
            Station officeStation = railwayData.dataCache.platformIdToStation.get(officePlatformId);

            // 既にキャッシュされたルートがあれば使う（HousingBlockEntityにキャッシュ実装があれば）
            List<Long> cachedRoute = housingBlockEntity.getCachedRoute();
            if (cachedRoute != null && !cachedRoute.isEmpty()) {
                // 乗客を直接生成。キャッシュがあれば非同期検索省略
                spawnPassengerWithRoute(cachedRoute, homePos, newId, name, world);
                player.sendMessage(Text.literal("キャッシュされたルートで乗客を生成しました。"), false);
                return ActionResult.SUCCESS;
            }

            // キャッシュなければ非同期で経路検索
            railwayData.railwayDataRouteFinderModule.findRoute(homeStation.getCenter(), officeStation.getCenter(), maxTickTime, (routeFinderDataList, duration) -> {
                System.out.println("== コールバック開始 ==");
                try {
                    if (routeFinderDataList == null || routeFinderDataList.isEmpty()) {
                        player.sendMessage(Text.literal("ルートが見つかりませんでした。"), false);
                        return;
                    }

                    List<Long> platformIdList = new java.util.ArrayList<>();
                    for (var data : routeFinderDataList) {
                        Long platId = railwayData.dataCache.blockPosToPlatformId.get(data.pos.asLong());
                        var platform = (platId != null) ? railwayData.dataCache.platformIdMap.get(platId) : null;
                        platformIdList.add(platform != null ? platform.id : -1L);
                    }

                    // キャッシュに保存
                    housingBlockEntity.setCachedRoute(platformIdList);

                    spawnPassengerWithRoute(platformIdList, homePos, newId, name, world);
                    player.sendMessage(Text.literal("新規ルートで乗客を生成しました。"), false);
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(Text.literal("例外が発生しました: " + e.getMessage()), false);
                }
            });
        }
        return ActionResult.SUCCESS;
    }

    private void spawnPassengerWithRoute(List<Long> platformIdList, BlockPos homePos, long newId, String name, World world) {
        double x = homePos.getX() + 0.5;
        double y = homePos.getY() + 1.0;
        double z = homePos.getZ() + 0.5;

        if (!platformIdList.isEmpty() && platformIdList.get(0) != -1L) {
            var railwayData = mtr.data.RailwayData.getInstance((ServerWorld) world);
            if (railwayData != null) {
                var firstPlatform = railwayData.dataCache.platformIdMap.get(platformIdList.get(0));
                if (firstPlatform != null) {
                    BlockPos platPos = firstPlatform.getMidPos();
                    x = platPos.getX() + 0.5;
                    y = platPos.getY();
                    z = platPos.getZ() + 0.5;
                }
            }
        }

        Passenger passenger = new Passenger(newId, name, x, y, z, 0xFFFFFF);
        passenger.route = platformIdList;
        passenger.routeTargetIndex = 0;
        passenger.moveState = Passenger.MoveState.WALKING_TO_PLATFORM;

        synchronized (PassengerManager.PASSENGER_LIST) {
            PassengerManager.PASSENGER_LIST.add(passenger);
        }
        PassengerManager.save();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                  BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, Rcap.HOUSING_BLOCK_ENTITY, HousingBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL ; }
}
