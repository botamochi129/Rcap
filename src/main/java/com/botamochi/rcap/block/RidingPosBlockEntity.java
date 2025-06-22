package com.botamochi.rcap.block;

import com.botamochi.rcap.registry.RcapBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class RidingPosBlockEntity extends BlockEntity {
    private int platformId = 1;

    public RidingPosBlockEntity(BlockPos pos, BlockState state) {
        super(RcapBlockEntities.RIDING_POS, pos, state);
    }

    public void setPlatformId(int value) {
        platformId = value;
        markDirty();
    }

    public int getPlatformId() {
        return platformId;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        platformId = nbt.getInt("platformId");
        if (platformId < 1) platformId = 1;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("platformId", platformId);
    }
}