package com.botamochi.rcap.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class OfficeBlockEntity extends BlockEntity {
    public OfficeBlockEntity(BlockPos pos, BlockState state) {
        super(RcapBlockEntity.OFFICE_BLOCK_ENTITY, pos, state);
    }
}
