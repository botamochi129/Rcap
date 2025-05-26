package com.botamochi.rcap;

import com.botamochi.rcap.block.BoardingPosBlock;
import com.botamochi.rcap.block.EntranceBlock;
import com.botamochi.rcap.block.ExitBlock;
import com.botamochi.rcap.data.PassengerManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Rcap implements ModInitializer {
    public static final String MOD_ID = "rcap";
    public static PassengerManager passengerManager;

    public static final Block ENTRANCE_BLOCK = new EntranceBlock();
    public static final Block EXIT_BLOCK = new ExitBlock();
    public static final Block BOARDING_POS_BLOCK = new BoardingPosBlock();

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "entrance_block"), ENTRANCE_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "entrance_block"), new BlockItem(ENTRANCE_BLOCK, new Item.Settings()));
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "exit_block"), EXIT_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "exit_block"), new BlockItem(EXIT_BLOCK, new Item.Settings()));
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "boarding_pos_block"), BOARDING_POS_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "boarding_pos_block"), new BlockItem(BOARDING_POS_BLOCK, new Item.Settings()));

        passengerManager = new PassengerManager();

        ServerTickEvents.END_WORLD_TICK.register((ServerWorld world) -> {
            passengerManager.tick(world);
            passengerManager.sendSyncPacketToAll(world);
        });
    }
}