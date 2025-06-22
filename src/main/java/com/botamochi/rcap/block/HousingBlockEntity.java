package com.botamochi.rcap.block;

import com.botamochi.rcap.registry.RcapBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class HousingBlockEntity extends BlockEntity {
    private int residents = 1;

    public HousingBlockEntity(BlockPos pos, BlockState state) {
        super(RcapBlockEntities.HOUSING, pos, state);
    }

    public void setResidents(int value) {
        residents = value;
        markDirty();
    }

    public int getResidents() {
        return residents;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        residents = nbt.getInt("residents");
        if (residents < 1) residents = 1;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("residents", residents);
    }
}