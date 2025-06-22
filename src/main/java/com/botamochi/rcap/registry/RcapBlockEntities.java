package com.botamochi.rcap.registry;

import com.botamochi.rcap.block.*;
import com.botamochi.rcap.Rcap;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RcapBlockEntities {
    public static BlockEntityType<HousingBlockEntity> HOUSING;
    public static BlockEntityType<OfficeBlockEntity> OFFICE;
    public static BlockEntityType<RidingPosBlockEntity> RIDING_POS;

    public static void register() {
        HOUSING = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier(Rcap.MOD_ID, "housing_block_entity"),
                BlockEntityType.Builder.create(HousingBlockEntity::new, RcapBlocks.HOUSING_BLOCK).build(null));
        OFFICE = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier(Rcap.MOD_ID, "office_block_entity"),
                BlockEntityType.Builder.create(OfficeBlockEntity::new, RcapBlocks.OFFICE_BLOCK).build(null));
        RIDING_POS = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier(Rcap.MOD_ID, "riding_pos_block_entity"),
                BlockEntityType.Builder.create(RidingPosBlockEntity::new, RcapBlocks.RIDING_POS_BLOCK).build(null));
    }
}