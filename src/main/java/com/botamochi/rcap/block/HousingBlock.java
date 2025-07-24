package com.botamochi.rcap.block;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.block.entity.HousingBlockEntity;

import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import mtr.data.Platform;
import mtr.data.RailwayData;
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
            if (!(be instanceof HousingBlockEntity)) {
                return ActionResult.SUCCESS;
            }

            player.openHandledScreen((HousingBlockEntity) be);

            com.botamochi.rcap.block.entity.OfficeBlockEntity office = com.botamochi.rcap.data.OfficeManager.getRandomAvailableOffice();
            if (office == null) {
                player.sendMessage(Text.literal("利用可能なオフィスが見つかりません。"), false);
                return ActionResult.SUCCESS;
            }

            if (!(world instanceof ServerWorld serverWorld)) {
                player.sendMessage(Text.literal("サーバーワールドでのみ動作します。"), false);
                return ActionResult.SUCCESS;
            }

            var railwayData = mtr.data.RailwayData.getInstance(serverWorld);
            if (railwayData == null) {
                player.sendMessage(Text.literal("RailwayDataが取得できません。"), false);
                return ActionResult.SUCCESS;
            }
            if (railwayData.railwayDataRouteFinderModule == null) {
                player.sendMessage(Text.literal("経路検索モジュールが利用できません。"), false);
                return ActionResult.SUCCESS;
            }

            long newId = System.currentTimeMillis();
            String name = "テスト乗客";

            BlockPos homePos = pos;
            BlockPos officePos = office.getPos();

            System.out.println("[HousingBlock] findRoute開始 homePos=" + homePos + " officePos=" + officePos);
            player.sendMessage(Text.literal("[HousingBlock] findRoute開始"), false);

            int maxTickTime = 40;

            long homePlatformId = RailwayData.getClosePlatformId(railwayData.platforms, railwayData.dataCache, homePos, 1000, -64, 320);
            long officePlatformId = RailwayData.getClosePlatformId(railwayData.platforms, railwayData.dataCache, officePos, 1000, -64, 320);

            Station homestation = railwayData.dataCache.platformIdToStation.get(homePlatformId);
            Station officestation = railwayData.dataCache.platformIdToStation.get(officePlatformId);
            System.out.println("homePlatId = " + homePlatformId + ", officePlatId = " + officePlatformId);
            System.out.println("homeStation = " + homestation + ", officeStation = " + officestation);

            railwayData.railwayDataRouteFinderModule.findRoute(homestation.getCenter(), officestation.getCenter(), maxTickTime, (routeFinderDataList, duration) -> {
                try {
                    System.out.println("[HousingBlock] Route callback called, route size: " + (routeFinderDataList == null ? "null" : routeFinderDataList.size()));
                    player.sendMessage(Text.literal("ルート検索コールバック呼ばれました。"), false);

                    if (routeFinderDataList == null || routeFinderDataList.isEmpty()) {
                        player.sendMessage(Text.literal("ルートが見つかりませんでした。"), false);
                        return;
                    }

                    List<Long> platformIdList = new java.util.ArrayList<>();
                    for (var data : routeFinderDataList) {
                        Long platId = railwayData.dataCache.blockPosToPlatformId.get(data.pos.asLong());
                        var platform = (platId != null) ? railwayData.dataCache.platformIdMap.get(platId) : null;
                        if (platform != null) {
                            platformIdList.add(platform.id);
                        } else {
                            platformIdList.add(-1L);
                        }
                    }

                    // 初期位置設定
                    double x = homePos.getX() + 0.5;
                    double y = homePos.getY() + 1.0;
                    double z = homePos.getZ() + 0.5;

                    if (!platformIdList.isEmpty() && platformIdList.get(0) != -1L) {
                        var firstPlatform = railwayData.dataCache.platformIdMap.get(platformIdList.get(0));
                        if (firstPlatform != null) {
                            BlockPos platPos = firstPlatform.getMidPos();
                            x = platPos.getX() + 0.5;
                            y = platPos.getY();
                            z = platPos.getZ() + 0.5;
                        }
                    }

                    Passenger passenger = new Passenger(newId, name, x, y, z, 0xFFFFFF);
                    passenger.route = platformIdList;
                    passenger.routeTargetIndex = 0;
                    passenger.moveState = Passenger.MoveState.WALKING_TO_PLATFORM;

                    // 非同期スレッドからは直接の追加は避け、追加用キューへ入れるだけにする
                    PassengerManager.PENDING_ADD_QUEUE.add(passenger);
                    PassengerManager.save();

                    // サーバー側Tickで処理されるため同期は不要
                    player.sendMessage(Text.literal("乗客をキューに追加しました。サーバーTickで反映されます。"), false);
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(Text.literal("例外が発生しました: " + e.getMessage()), false);
                }
            });

        }
        return ActionResult.SUCCESS;
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
