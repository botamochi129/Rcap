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
        // 日付管理
        long currentDay = worldTime / 24000L;
        if (lastSpawnDay != currentDay) {
            spawnedToday = 0;
            lastSpawnDay = (int) currentDay;
        }
        if (spawnedToday >= householdSize) return;

        OfficeBlockEntity office = OfficeManager.getRandomAvailableOffice();
        if (office == null) return;

        // RailwayData とルート探索モジュールを取得
        RailwayData railwayData = mtr.data.RailwayData.getInstance(world);
        if (railwayData == null || railwayData.railwayDataRouteFinderModule == null) return;

        BlockPos homePos = this.pos;
        BlockPos officePos = office.getPos();

        int maxTickTime = 40;  // 適宜調整

        // 非同期ルート検索の呼び出し
        railwayData.railwayDataRouteFinderModule.findRoute(homePos, officePos, maxTickTime, (routeFinderDataList, duration) -> {
            if (routeFinderDataList == null || routeFinderDataList.isEmpty()) {
                // 経路無しまたは失敗時は生成しない（必要なら別途処理）
                return;
            }

            // 取得したルート情報からプラットフォームIDリストを作成
            List<Long> platformIdList = new ArrayList<>();
            for (var data : routeFinderDataList) {
                var platform = railwayData.dataCache.platformIdMap.get(railwayData.dataCache.blockPosToPlatformId.get(data.pos.asLong()));
                if (platform != null) {
                    platformIdList.add(platform.id);
                } else {
                    platformIdList.add(-1L); // 不明プラットフォームIDは-1など適宜ハンドリング
                }
            }

            // 乗客初期位置はルートの最初のプラットフォームか住宅中心付近
            double startX = homePos.getX() + 0.5;
            double startY = homePos.getY() + 1.0;
            double startZ = homePos.getZ() + 0.5;

            if (!platformIdList.isEmpty()) {
                long firstPlatformId = platformIdList.get(0);
                var platform = railwayData.dataCache.platformIdMap.get(firstPlatformId);
                if (platform != null) {
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

            passenger.route = platformIdList;
            passenger.routeTargetIndex = 0;
            passenger.moveState = Passenger.MoveState.WALKING_TO_PLATFORM;

            // 乗客リスト追加は同期化
            synchronized (PassengerManager.PASSENGER_LIST) {
                PassengerManager.PASSENGER_LIST.add(passenger);
            }
            PassengerManager.save();

            spawnedToday++;
            this.markDirty();
        });
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
