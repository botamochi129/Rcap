package com.botamochi.rcap.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public class HousingBlockEntity extends BlockEntity {
    public HousingBlockEntity(BlockPos pos, BlockState state) {
        super(RcapBlockEntity.HOUSING_BLOCK_ENTITY, pos, state);
    }
}
