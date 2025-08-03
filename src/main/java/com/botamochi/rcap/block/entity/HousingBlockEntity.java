package com.botamochi.rcap.block.entity;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.data.HousingManager;
import com.botamochi.rcap.data.OfficeManager;
import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import com.botamochi.rcap.screen.HousingBlockScreenHandler;
import mtr.data.Platform;
import mtr.data.RailwayData;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class HousingBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {

    private int householdSize = 1;
    private int spawnedToday = 0;
    private int lastSpawnDay = -1;

    private Long linkedOfficePosLong = null;
    private List<Long> cachedRoute = null;

    // ★追加★ 最終乗客生成Tick管理（負荷軽減）
    private long lastSpawnTick = -1L;

    public HousingBlockEntity(BlockPos pos, BlockState state) {
        super(Rcap.HOUSING_BLOCK_ENTITY, pos, state);
    }

    public int getHouseholdSize() {
        return householdSize;
    }

    public void setHouseholdSize(int size) {
        this.householdSize = size;
        markDirty();
        System.out.println("【保存】householdSize = " + size);
    }

    public Long getLinkedOfficePosLong() {
        return linkedOfficePosLong;
    }

    public List<Long> getCachedRoute() {
        return cachedRoute;
    }

    public void setCachedRoute(List<Long> route) {
        this.cachedRoute = route;
        markDirty();
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("HouseholdSize", householdSize);

        if (linkedOfficePosLong != null) {
            nbt.putLong("LinkedOfficePos", linkedOfficePosLong);
        } else {
            nbt.remove("LinkedOfficePos");
        }

        if (cachedRoute != null && !cachedRoute.isEmpty()) {
            long[] routeArray = cachedRoute.stream().mapToLong(Long::longValue).toArray();
            nbt.putLongArray("CachedRoute", routeArray);
        } else {
            nbt.remove("CachedRoute");
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        householdSize = nbt.getInt("HouseholdSize");

        if (nbt.contains("LinkedOfficePos")) {
            linkedOfficePosLong = nbt.getLong("LinkedOfficePos");
        } else {
            linkedOfficePosLong = null;
        }

        if (nbt.contains("CachedRoute")) {
            long[] routeArray = nbt.getLongArray("CachedRoute");
            cachedRoute = new ArrayList<>(routeArray.length);
            for (long l : routeArray) {
                cachedRoute.add(l);
            }
        } else {
            cachedRoute = null;
        }
    }

    public void setLinkedOfficePosLong(Long posLong) {
        this.linkedOfficePosLong = posLong;
        markDirty();

        if (posLong != null && this.world instanceof ServerWorld serverWorld) {
            RailwayData railwayData = RailwayData.getInstance(serverWorld);
            long homePid = RailwayData.getClosePlatformId(railwayData.platforms, railwayData.dataCache, this.pos, 2000, 1000, 1000);
            Platform homePlatform = railwayData.dataCache.platformIdMap.get(homePid);
            BlockPos homePlatformPos = (homePlatform != null) ? homePlatform.getMidPos() : null;

            BlockPos officeBlockPos = BlockPos.fromLong(posLong);
            long officePid = RailwayData.getClosePlatformId(railwayData.platforms, railwayData.dataCache, officeBlockPos, 2000, 1000, 1000);
            Platform officePlatform = railwayData.dataCache.platformIdMap.get(officePid);
            BlockPos officePlatformPos = (officePlatform != null) ? officePlatform.getMidPos() : null;

            if (homePlatformPos == null || officePlatformPos == null) {
                System.out.println("[HousingBlockEntity] 有効なプラットフォームが見つからず経路検索できません, homePid=" + homePid + ", officePid=" + officePid);
                this.cachedRoute = null;
                markDirty();
                return;
            }

            if (railwayData != null && railwayData.railwayDataRouteFinderModule != null) {
                boolean queued = railwayData.railwayDataRouteFinderModule.findRoute(
                        homePlatformPos, officePlatformPos, 60,
                        (routeFinderDataList, duration) -> {
                            System.out.println("[RouteFinder Callback] called, routeFinderDataList.size=" + (routeFinderDataList == null ? 0 : routeFinderDataList.size()));
                            if (routeFinderDataList == null || routeFinderDataList.isEmpty()) {
                                this.cachedRoute = null;
                                markDirty();
                                System.out.println("[HousingBlockEntity] 経路検索コールバック結果がnullまたは空");
                                return;
                            }
                            List<Long> platformIdList = new ArrayList<>();
                            for (var data : routeFinderDataList) {
                                Long pid = railwayData.dataCache.blockPosToPlatformId.get(data.pos.asLong());
                                if (pid != null && pid >= 0) platformIdList.add(pid);
                            }
                            if (!platformIdList.isEmpty()) {
                                setCachedRoute(platformIdList);
                                System.out.println("[HousingBlockEntity] 経路キャッシュ完了: " + platformIdList);
                            } else {
                                this.cachedRoute = null;
                                markDirty();
                                System.out.println("[HousingBlockEntity] プラットフォームIDリストが空");
                            }
                        }
                );
                if (!queued) {
                    System.out.println("[HousingBlockEntity] findRoute呼び出し失敗：検索キューが満杯の可能性");
                } else {
                    System.out.println("[HousingBlockEntity] findRoute呼び出し成功：経路検索をキューに追加");
                }
            }
        }
    }

    public void spawnPassengersIfTime(World world, long worldTime) {
        System.out.println("[spawnPassengersIfTime] worldTime=" + worldTime + ", householdSize=" + householdSize + ", spawnedToday=" + spawnedToday + ", cachedRoute=" + cachedRoute);

        long currentDay = worldTime / 24000L;
        if (lastSpawnDay != currentDay) {
            spawnedToday = 0;
            lastSpawnDay = (int) currentDay;
            System.out.println("[spawnPassengersIfTime] new day detected. Reset spawnedToday");
        }

        if (spawnedToday >= householdSize) {
            System.out.println("[spawnPassengersIfTime] spawnedToday reached householdSize, skipping spawn");
            return;
        }

        if (cachedRoute == null || cachedRoute.isEmpty()) {
            System.out.println("[spawnPassengersIfTime] cachedRoute is null or empty, skipping spawn");
            return;
        }

        while (spawnedToday < householdSize) {
            spawnPassengerWithRoute(cachedRoute, this.pos, spawnedToday);
            spawnedToday++;
            markDirty();
        }
    }

    private void spawnPassengerWithRoute(List<Long> platformIdList, BlockPos homePos, int seq) {
        System.out.println("[spawnPassengerWithRoute] called seq=" + seq + ", platformIds=" + platformIdList);
        double x = homePos.getX() + 0.5, y = homePos.getY() + 1.0, z = homePos.getZ() + 0.5;
        if (!platformIdList.isEmpty() && platformIdList.get(0) != -1L) {
            var world = this.getWorld();
            if (world instanceof ServerWorld serverWorld) {
                var railwayData = RailwayData.getInstance(serverWorld);
                var firstPlatform = railwayData.dataCache.platformIdMap.get(platformIdList.get(0));
                if (firstPlatform != null) {
                    BlockPos platPos = firstPlatform.getMidPos();
                    x = platPos.getX() + 0.5;
                    y = platPos.getY();
                    z = platPos.getZ() + 0.5;
                }
            }
        }
        Passenger passenger = new Passenger(System.currentTimeMillis(), "Passenger-" + seq, x, y, z, 0xFFFFFF);
        passenger.route = platformIdList;
        passenger.routeTargetIndex = 0;
        passenger.moveState = Passenger.MoveState.WALKING_TO_PLATFORM;
        synchronized (PassengerManager.PASSENGER_LIST) {
            PassengerManager.PASSENGER_LIST.add(passenger);
        }
        PassengerManager.save();
    }

    public static List<HousingBlockEntity> getAllHousingBlocks(MinecraftServer server) {
        List<HousingBlockEntity> result = new ArrayList<>();
        for (ServerWorld world : server.getWorlds()) {
            for (BlockPos pos : BlockPos.iterate(
                    world.getBottomY(), 0, world.getBottomY(),
                    world.getTopY() - 1, 255, world.getTopY() - 1)) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof HousingBlockEntity hbe) {
                    result.add(hbe);
                }
            }
        }
        return result;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("世帯人数設定");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
        return new HousingBlockScreenHandler(syncId, inventory, this.getPos(), this.householdSize);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.getPos());
        buf.writeInt(this.householdSize);
    }

    public static void tick(World world, BlockPos pos, BlockState state, HousingBlockEntity blockEntity) {
        HousingManager.registerHousing(pos);
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (world != null && !world.isClient) {
            HousingManager.unregisterHousing(pos);
        }
    }
}
