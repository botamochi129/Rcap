package com.botamochi.rcap.block.entity;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.data.HousingManager;
import com.botamochi.rcap.data.OfficeManager;
import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import com.botamochi.rcap.screen.HousingBlockScreenHandler;
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

    public int getHouseholdSize() { return householdSize; }
    public void setHouseholdSize(int size) {
        this.householdSize = size;
        markDirty();
        System.out.println("【保存】householdSize = " + size);
    }

    public Long getLinkedOfficePosLong() { return linkedOfficePosLong; }
    public void setLinkedOfficePosLong(Long posLong) {
        this.linkedOfficePosLong = posLong;
        markDirty();
    }

    public List<Long> getCachedRoute() { return cachedRoute; }
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

    public void spawnPassengersIfTime(ServerWorld world, long worldTime) {
        long currentDay = worldTime / 24000L;
        if (lastSpawnDay != currentDay) {
            spawnedToday = 0;
            lastSpawnDay = (int) currentDay;
        }
        if (spawnedToday >= householdSize) return;

        long currentTick = world.getTime();

        // ★ 1：経路検索呼び出し間隔制限（例:最低100tick=約5秒の間隔）
        final int MIN_ROUTE_SEARCH_INTERVAL_TICKS = 100;
        if (lastSpawnTick >= 0 && currentTick - lastSpawnTick < MIN_ROUTE_SEARCH_INTERVAL_TICKS) return;
        lastSpawnTick = currentTick;

        // ★ 2：フォールバック乗客最大数制限（例:最大3体まで）
        int fallbackCount = 0;
        synchronized (PassengerManager.PASSENGER_LIST) {
            for (Passenger p : PassengerManager.PASSENGER_LIST) {
                if (p.name.startsWith("QueueFallback")) fallbackCount++;
            }
        }
        final int MAX_FALLBACK_PASSENGERS = 3;
        if (fallbackCount >= MAX_FALLBACK_PASSENGERS) {
            // フォールバック乗客が多すぎる場合は生成抑制
            return;
        }

        OfficeBlockEntity office = null;
        if (linkedOfficePosLong != null) {
            office = OfficeManager.getAll().stream()
                    .filter(o -> o.getPos().asLong() == linkedOfficePosLong)
                    .findFirst()
                    .orElse(null);
        }
        if (office == null) {
            office = OfficeManager.getRandomAvailableOffice();
            if (office == null) return;
            setLinkedOfficePosLong(office.getPos().asLong());
        }

        RailwayData railwayData = RailwayData.getInstance(world);
        if (railwayData == null || railwayData.railwayDataRouteFinderModule == null) return;

        BlockPos homePos = this.pos;
        BlockPos officePos = office.getPos();

        int maxTickTime = 40;

        if (cachedRoute != null && !cachedRoute.isEmpty()) {
            spawnPassengerWithRoute(cachedRoute, homePos, spawnedToday);
            spawnedToday++;
            markDirty();
            return;
        }

        boolean queued = railwayData.railwayDataRouteFinderModule.findRoute(homePos, officePos, maxTickTime, (routeFinderDataList, duration) -> {
            if (routeFinderDataList == null || routeFinderDataList.isEmpty()) return;

            List<Long> platformIdList = new ArrayList<>();
            for (var data : routeFinderDataList) {
                Long platformId = railwayData.dataCache.blockPosToPlatformId.get(data.pos.asLong());
                platformIdList.add(platformId != null ? platformId : -1L);
            }

            setCachedRoute(platformIdList);

            spawnPassengerWithRoute(platformIdList, homePos, spawnedToday);
            spawnedToday++;
            markDirty();
        });

        if (!queued) {
            double x = homePos.getX() + 0.5;
            double y = homePos.getY() + 1.0;
            double z = homePos.getZ() + 0.5;

            List<Long> fallbackRoute = new ArrayList<>();
            if (railwayData != null && railwayData.dataCache.platformIdMap != null && !railwayData.dataCache.platformIdMap.isEmpty()) {
                Long firstPlatformId = railwayData.dataCache.platformIdMap.keySet().iterator().next();
                fallbackRoute.add(firstPlatformId);
            }

            Passenger fallbackPassenger = new Passenger(System.currentTimeMillis(), "QueueFallback", x, y, z, 0xFFFFFF);
            fallbackPassenger.route = fallbackRoute;
            fallbackPassenger.routeTargetIndex = 0;
            fallbackPassenger.moveState = Passenger.MoveState.WALKING_TO_PLATFORM;

            PassengerManager.PENDING_ADD_QUEUE.offer(fallbackPassenger);
            PassengerManager.save();

            spawnedToday++;
            markDirty();
        }
    }

    private void spawnPassengerWithRoute(List<Long> platformIdList, BlockPos homePos, int seq) {
        double x = homePos.getX() + 0.5;
        double y = homePos.getY() + 1.0;
        double z = homePos.getZ() + 0.5;

        if (!platformIdList.isEmpty() && platformIdList.get(0) != -1L) {
            var railwayData = RailwayData.getInstance((ServerWorld) this.getWorld());
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

        Passenger passenger = new Passenger(
                System.currentTimeMillis(),
                "Passenger-" + seq,
                x, y, z,
                0xFFFFFF
        );

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
