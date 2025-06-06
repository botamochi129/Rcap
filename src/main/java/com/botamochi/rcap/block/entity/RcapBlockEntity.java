package com.botamochi.rcap.block.entity;

import com.botamochi.rcap.Rcap;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RcapBlockEntity {
    public static BlockEntityType<HousingBlockEntity> HOUSING_BLOCK_ENTITY;
    public static BlockEntityType<OfficeBlockEntity> OFFICE_BLOCK_ENTITY;
    public static BlockEntityType<RidingPosBlockEntity> RIDING_POS_BLOCK_ENTITY;

    public static void registerAll() {
        HOUSING_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier(Rcap.MOD_ID, "housing_block_entity"),
                FabricBlockEntityTypeBuilder.create(HousingBlockEntity::new, Rcap.HOUSING_BLOCK).build(null)
        );
        OFFICE_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier(Rcap.MOD_ID, "office_block_entity"),
                FabricBlockEntityTypeBuilder.create(OfficeBlockEntity::new, Rcap.OFFICE_BLOCK).build(null)
        );
        RIDING_POS_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier(Rcap.MOD_ID, "riding_pos_block_entity"),
                FabricBlockEntityTypeBuilder.create(RidingPosBlockEntity::new, Rcap.RIDING_POS_BLOCK).build(null)
        );
    }
}
