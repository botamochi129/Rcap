package com.botamochi.rcap.block.entity;

import com.botamochi.rcap.Rcap;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class HousingBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
    private int householdSize = 1;

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
