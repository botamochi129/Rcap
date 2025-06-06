package com.botamochi.rcap.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class RidingPosBlockEntity extends BlockEntity {
    public RidingPosBlockEntity(BlockPos pos, BlockState state) {
        super(RcapBlockEntity.RIDING_POS_BLOCK_ENTITY, pos, state);
    }
}
