package com.botamochi.rcap.block.entity;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.data.HousingManager;
import com.botamochi.rcap.data.OfficeManager;
import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import com.botamochi.rcap.passenger.PassengerRouteFinder;
import com.botamochi.rcap.screen.HousingBlockScreenHandler;
import com.botamochi.rcap.screen.ModScreens;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
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
import java.util.UUID;

public class HousingBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
    private int householdSize = 1;
    private int spawnedToday = 0;

    public HousingBlockEntity(BlockPos pos, BlockState state) {
        super(Rcap.HOUSING_BLOCK_ENTITY, pos, state);
    }

    public int getHouseholdSize() {
        return householdSize;
    }

    public void setHouseholdSize(int size) {
        this.householdSize = size;
        System.out.println("【保存】householdSize = " + size);
        markDirty();
    }

    private int lastSpawnDay = -1; // 現実「日」（0〜365）

    public void spawnPassengersIfTime(ServerWorld world, long worldTime) {
        // ワールド時間を日数に変換（1日 = 24000tick）
        long currentDay = worldTime / 24000L;

        if (lastSpawnDay != currentDay) {
            spawnedToday = 0;
            lastSpawnDay = (int) currentDay;
        }

        if (spawnedToday >= householdSize) return;

        // オフィスを取得
        OfficeBlockEntity office = OfficeManager.getRandomAvailableOffice();
        if (office == null) return;

        // 住宅とオフィスの座標のlong値を取得
        long homePosLong = this.pos.asLong();
        long officePosLong = office.getPos().asLong();

        // ルート検索（MTRのプラットフォームIDリストを取得）
        List<Long> route = PassengerRouteFinder.findRoute(world, homePosLong, officePosLong);

        // ルートが存在し、最初のプラットフォーム座標を取得
        double startX = pos.getX() + 0.5;
        double startY = pos.getY() + 1.0;
        double startZ = pos.getZ() + 0.5;

        if (!route.isEmpty()) {
            long firstPlatformId = route.get(0);
            var railwayData = mtr.data.RailwayData.getInstance(world);
            if (railwayData != null && railwayData.dataCache.platformIdMap.containsKey(firstPlatformId)) {
                var platform = railwayData.dataCache.platformIdMap.get(firstPlatformId);
                BlockPos platPos = platform.getMidPos();
                startX = platPos.getX() + 0.5;
                startY = platPos.getY();
                startZ = platPos.getZ() + 0.5;
            }
        }

        Passenger passenger = new Passenger(
                System.currentTimeMillis(),
                "Passenger-" + spawnedToday,
                startX, startY, startZ,
                0xFFFFFF
        );

        // ルート・状態を設定
        passenger.route = route;
        passenger.routeTargetIndex = 0;
        passenger.moveState = Passenger.MoveState.WALKING_TO_PLATFORM;

        PassengerManager.PASSENGER_LIST.add(passenger);
        PassengerManager.save();

        spawnedToday++;
        markDirty();
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
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("HouseholdSize", householdSize);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        householdSize = nbt.getInt("HouseholdSize");
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
        buf.writeInt(this.householdSize); // ← これを追加
    }

    public static void tick(World world, BlockPos pos, BlockState state, HousingBlockEntity blockEntity) {
        HousingManager.registerHousing(pos); // 一度だけ登録してもOK（条件付き）
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (!world.isClient) {
            HousingManager.unregisterHousing(pos);
        }
    }
}
