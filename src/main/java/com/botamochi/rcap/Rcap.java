package com.botamochi.rcap;

import com.botamochi.rcap.block.HousingBlock;
import com.botamochi.rcap.block.OfficeBlock;
import com.botamochi.rcap.block.RidingPosBlock;
import com.botamochi.rcap.block.entity.HousingBlockEntity;
import com.botamochi.rcap.block.entity.RcapBlockEntity;
import com.botamochi.rcap.passenger.PassengerManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.mtr.mod.client.MinecraftClientData;

public class Rcap implements ModInitializer {
    public static final String MOD_ID = "rcap";

    public static final Block HOUSING_BLOCK = new HousingBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f));
    public static final Block OFFICE_BLOCK = new OfficeBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f));
    public static final Block RIDING_POS_BLOCK = new RidingPosBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f));

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "housing_block"), HOUSING_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "housing_block"), new BlockItem(HOUSING_BLOCK, new Item.Settings()));
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "office_block"), OFFICE_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "office_block"), new BlockItem(OFFICE_BLOCK, new Item.Settings()));
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "riding_pos_block"), RIDING_POS_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "riding_pos_block"), new BlockItem(RIDING_POS_BLOCK, new Item.Settings()));

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            PassengerManager.tickAll(world, MinecraftClientData.getInstance());
        });

        RcapBlockEntity.registerAll();
    }
}
