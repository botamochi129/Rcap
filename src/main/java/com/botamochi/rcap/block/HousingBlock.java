package com.botamochi.rcap.block;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.block.entity.HousingBlockEntity;

import com.botamochi.rcap.block.entity.OfficeBlockEntity;
import com.botamochi.rcap.data.OfficeManager;
import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

            // ランダムオフィス取得
            OfficeBlockEntity office = null;
            Long linkedOfficePosLong = housingBlockEntity.getLinkedOfficePosLong();
            if (linkedOfficePosLong != null) {
                office = OfficeManager.getAll().stream()
                        .filter(o -> o.getPos().asLong() == linkedOfficePosLong)
                        .findFirst()
                        .orElse(null);
            }

            if (office == null) {
                office = OfficeManager.getRandomAvailableOffice();
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

            var railwayData = RailwayData.getInstance(serverWorld);
            if (railwayData == null || railwayData.railwayDataRouteFinderModule == null) {
                player.sendMessage(Text.literal("RailwayDataまたは経路検索モジュールが取得できません。"), false);
                return ActionResult.SUCCESS;
            }

            BlockPos homePos = pos;
            BlockPos officePos = office.getPos();

            player.sendMessage(Text.literal("[HousingBlock] 乗客生成テスト開始"), false);
            player.sendMessage(Text.literal("[HousingBlock] 経路検索を開始します…"), false);

            List<Long> cachedRoute = housingBlockEntity.getCachedRoute();

            if (cachedRoute != null && !cachedRoute.isEmpty()) {
                long newId = System.currentTimeMillis();
                spawnPassengerWithRoute(cachedRoute, homePos, newId, "CachedPassenger" + newId, world);
                player.sendMessage(Text.literal("[HousingBlock] キャッシュされたルートで乗客を生成しました。"), false);
                player.sendMessage(Text.literal("[HousingBlock] 現在の乗客数: " + PassengerManager.PASSENGER_LIST.size()), false);
                return ActionResult.SUCCESS;
            }

            boolean queued = railwayData.railwayDataRouteFinderModule.findRoute(
                    homePos,
                    officePos,
                    40,
                    (List<mtr.data.RailwayDataRouteFinderModule.RouteFinderData> dataList, Integer duration) -> {
                        try {
                            player.sendMessage(Text.literal("[HousingBlock] 経路検索コールバック呼び出し"), false);

                            if (dataList == null) {
                                player.sendMessage(Text.literal("[HousingBlock] 経路検索結果: null"), false);
                                return;
                            }
                            player.sendMessage(Text.literal("[HousingBlock] 経路検索結果数: " + dataList.size()), false);

                            List<Long> platformIds = dataList.stream()
                                    .map(data -> railwayData.dataCache.blockPosToPlatformId.get(data.pos.asLong()))
                                    .filter(pid -> pid != null && pid >= 0)
                                    .collect(Collectors.toList());

                            player.sendMessage(Text.literal("[HousingBlock] プラットフォームID数: " + platformIds.size() + " 内容: " + platformIds), false);

                            if (platformIds.isEmpty()) {
                                double x = homePos.getX() + 0.5;
                                double y = homePos.getY() + 1.0;
                                double z = homePos.getZ() + 0.5;
                                Passenger fb = new Passenger(System.currentTimeMillis(), "FallbackPassenger", x, y, z, 0xFFFFFF);
                                fb.moveState = Passenger.MoveState.WALKING_TO_PLATFORM;
                                synchronized (PassengerManager.PASSENGER_LIST) {
                                    PassengerManager.PASSENGER_LIST.add(fb);
                                }
                                PassengerManager.save();
                                player.sendMessage(Text.literal("[HousingBlock] ルートなしフォールバック乗客生成。乗客数: " + PassengerManager.PASSENGER_LIST.size()), false);
                            } else {
                                housingBlockEntity.setCachedRoute(platformIds);
                                long newId = System.currentTimeMillis();
                                spawnPassengerWithRoute(platformIds, homePos, newId, "Passenger" + newId, world);
                                player.sendMessage(Text.literal("[HousingBlock] 経路検索完了、乗客生成 (所要 " + duration + " ms)。乗客数：" + PassengerManager.PASSENGER_LIST.size()), false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            player.sendMessage(Text.literal("[HousingBlock] 経路検索例外: " + e.getMessage()), false);
                        }
                    }
            );
            player.sendMessage(Text.literal("[HousingBlock] findRoute queued: " + queued), false);

            if (!queued) {
                player.sendMessage(Text.literal("[HousingBlock] 経路検索キューが満杯のため即フォールバック生成します。"), false);

                double x = homePos.getX() + 0.5;
                double y = homePos.getY() + 1.0;
                double z = homePos.getZ() + 0.5;

                List<Long> fallbackRoute = new ArrayList<>();
                if (railwayData != null && railwayData.dataCache.platformIdMap != null && !railwayData.dataCache.platformIdMap.isEmpty()) {
                    // 有効なプラットフォームIDの集合から最初の１つを取得
                    long validPlatformId = railwayData.dataCache.platformIdMap.keySet().iterator().next();
                    fallbackRoute.add(validPlatformId);
                } else {
                    // 万一なければ -1L など無効値を避けるかフォールバック自体を控える
                    // ここでの例外処理やログ出力も検討
                }

                Passenger fallbackPassenger = new Passenger(System.currentTimeMillis(), "QueueFallback", x, y, z, 0xFFFFFF);
                fallbackPassenger.route = fallbackRoute;
                fallbackPassenger.routeTargetIndex = 0;
                fallbackPassenger.moveState = Passenger.MoveState.WALKING_TO_PLATFORM;

                synchronized (PassengerManager.PASSENGER_LIST) {
                    PassengerManager.PASSENGER_LIST.add(fallbackPassenger);
                }
                PassengerManager.save();

                player.sendMessage(Text.literal("[HousingBlock] フォールバック乗客を生成しました。現在の乗客数：" + PassengerManager.PASSENGER_LIST.size()), false);
            }

            return ActionResult.SUCCESS;
        }
        return ActionResult.SUCCESS;
    }

    private void spawnPassengerWithRoute(List<Long> platformIdList, BlockPos homePos, long newId, String name, World world) {
        double x = homePos.getX() + 0.5;
        double y = homePos.getY() + 1.0;
        double z = homePos.getZ() + 0.5;

        if (!platformIdList.isEmpty() && platformIdList.get(0) != -1L) {
            var railwayData = RailwayData.getInstance((ServerWorld) world);
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
