package com.botamochi.rcap.block;

import com.botamochi.rcap.registry.RcapBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class OfficeBlockEntity extends BlockEntity {
    private int employees = 1;

    public OfficeBlockEntity(BlockPos pos, BlockState state) {
        super(RcapBlockEntities.OFFICE, pos, state);
    }

    public void setEmployees(int value) {
        employees = value;
        markDirty();
    }

    public int getEmployees() {
        return employees;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        employees = nbt.getInt("employees");
        if (employees < 1) employees = 1;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("employees", employees);
    }
}