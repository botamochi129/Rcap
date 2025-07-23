package com.botamochi.rcap.block.entity;

import com.botamochi.rcap.Rcap;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.Nullable;

public class RidingPosBlockEntity extends BlockEntity {
    private long platformId = -1L;

    public RidingPosBlockEntity(BlockPos pos, BlockState state) {
        super(Rcap.RIDING_POS_BLOCK_ENTITY, pos, state);
    }

    public long getPlatformId() {
        return platformId;
    }

    public void setPlatformId(long platformId) {
        this.platformId = platformId;
        markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong("PlatformId", platformId);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("PlatformId")) {
            this.platformId = nbt.getLong("PlatformId");
        }
    }
}
