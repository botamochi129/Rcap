package com.botamochi.rcap.registry;

import com.botamochi.rcap.block.*;
import com.botamochi.rcap.Rcap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RcapBlocks {
    public static final Block HOUSING_BLOCK = new HousingBlock(Block.Settings.copy(Blocks.STONE));
    public static final Block OFFICE_BLOCK = new OfficeBlock(Block.Settings.copy(Blocks.IRON_BLOCK));
    public static final Block RIDING_POS_BLOCK = new RidingPosBlock(Block.Settings.copy(Blocks.QUARTZ_BLOCK));

    public static void register() {
        Registry.register(Registry.BLOCK, new Identifier(Rcap.MOD_ID, "housing_block"), HOUSING_BLOCK);
        Registry.register(Registry.BLOCK, new Identifier(Rcap.MOD_ID, "office_block"), OFFICE_BLOCK);
        Registry.register(Registry.BLOCK, new Identifier(Rcap.MOD_ID, "riding_pos_block"), RIDING_POS_BLOCK);

        Registry.register(Registry.ITEM, new Identifier(Rcap.MOD_ID, "housing_block"),
                new BlockItem(HOUSING_BLOCK, new Item.Settings()));
        Registry.register(Registry.ITEM, new Identifier(Rcap.MOD_ID, "office_block"),
                new BlockItem(OFFICE_BLOCK, new Item.Settings()));
        Registry.register(Registry.ITEM, new Identifier(Rcap.MOD_ID, "riding_pos_block"),
                new BlockItem(RIDING_POS_BLOCK, new Item.Settings()));
    }
}