package com.botamochi.rcap;

import com.botamochi.rcap.block.HousingBlock;
import com.botamochi.rcap.block.OfficeBlock;
import com.botamochi.rcap.block.entity.HousingBlockEntity;
import com.botamochi.rcap.block.entity.OfficeBlockEntity;
import com.botamochi.rcap.data.CompanyManager;
import com.botamochi.rcap.network.HousingBlockPacketReceiver;
import com.botamochi.rcap.network.OfficeBlockPacketReceiver;
import com.botamochi.rcap.network.RcapServerPackets;
import com.botamochi.rcap.screen.ModScreens;
import com.botamochi.rcap.network.ServerNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;

public class Rcap implements ModInitializer {
    public static final String MOD_ID = "rcap";
    public static final Block HOUSING_BLOCK = new HousingBlock(FabricBlockSettings.of(Material.STONE).strength(2.0f));
    public static final Block OFFICE_BLOCK = new OfficeBlock(FabricBlockSettings.of(Material.STONE).strength(2.0f));
    public static BlockEntityType<OfficeBlockEntity> OFFICE_BLOCK_ENTITY;

    public static BlockEntityType<HousingBlockEntity> HOUSING_BLOCK_ENTITY;

    public static NbtCompound loadCompanyNBT(File file) {
        try {
            return NbtIo.read(file); // Minecraft 1.19+
        } catch (IOException e) {
            e.printStackTrace();
            return new NbtCompound(); // fallback
        }
    }


    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "housing_block"), HOUSING_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "housing_block"),
                new BlockItem(HOUSING_BLOCK, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));
        HOUSING_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier(MOD_ID, "housing_block_entity"),
                BlockEntityType.Builder.create(HousingBlockEntity::new, HOUSING_BLOCK).build(null)
        );

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "office_block"), OFFICE_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "office_block"), new BlockItem(OFFICE_BLOCK, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));
        OFFICE_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "office_block_entity"),
                BlockEntityType.Builder.create(OfficeBlockEntity::new, OFFICE_BLOCK).build(null)
        );

        ModScreens.registerScreenHandlers();
        RcapServerPackets.register();
        ServerNetworking.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld world = server.getOverworld();
            if (world != null) {
                CompanyManager.init(world);
            }
        });

        HousingBlockPacketReceiver.register();
        OfficeBlockPacketReceiver.register();
    }
}
