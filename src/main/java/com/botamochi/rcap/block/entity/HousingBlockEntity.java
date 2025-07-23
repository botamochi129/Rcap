package com.botamochi.rcap.block.entity;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.data.OfficeManager;
import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import com.botamochi.rcap.screen.HousingBlockScreenHandler;
import com.botamochi.rcap.screen.ModScreens;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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

    public void spawnPassengersIfTime(long now) {
        // 現在日を取得
        int currentDay = java.time.LocalDate.now().getDayOfYear();

        if (lastSpawnDay != currentDay) {
            spawnedToday = 0;
            lastSpawnDay = currentDay;
        }

        if (spawnedToday >= householdSize) return;

        OfficeBlockEntity office = OfficeManager.getRandomAvailableOffice();
        if (office == null) return;

        Passenger p = new Passenger(UUID.randomUUID(), getPos().asLong(), office.getPos().asLong());
        PassengerManager.addPassenger(p);
        office.assignPassenger(p.uuid); // 忘れずにオフィスへ割り当て
        spawnedToday++;
    }

    public static List<HousingBlockEntity> getAllHousingBlocks(MinecraftServer server) {
        List<HousingBlockEntity> result = new ArrayList<>();
        for (ServerWorld world : server.getWorlds()) {
            world.iterateEntities(null, entity -> false); // 不要なら削除
            for (BlockPos pos : BlockPos.iterate(
                    world.getBottomY(), world.getMinBuildHeight(),
                    world.getTopY() - 1, world.getMaxBuildHeight())) {
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
}
