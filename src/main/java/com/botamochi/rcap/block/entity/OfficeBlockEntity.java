package com.botamochi.rcap.block.entity;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.screen.OfficeBlockScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class OfficeBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
    private int staffCount = 1;

    public OfficeBlockEntity(BlockPos pos, BlockState state) {
        super(Rcap.OFFICE_BLOCK_ENTITY, pos, state);
    }

    public int getStaffCount() {
        return staffCount;
    }
    public void setStaffCount(int count) {
        this.staffCount = count;
        markDirty();
    }
    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("StaffCount", staffCount);
    }
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        staffCount = nbt.getInt("StaffCount");
    }
    @Override
    public Text getDisplayName() {
        return Text.literal("従業員人数設定");
    }
    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.getPos());
        buf.writeInt(this.staffCount);
    }
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
        return new OfficeBlockScreenHandler(syncId, inventory, this.getPos(), this.staffCount);
    }
}
